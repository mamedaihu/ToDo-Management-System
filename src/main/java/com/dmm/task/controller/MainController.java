package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.enttity.Tasks;
import com.dmm.task.form.TaskForm;
import com.dmm.task.repository.TasksRepository;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class MainController {
	
	@Autowired
	private TasksRepository repo;
	
	//カレンダー表示
	@GetMapping("/main")
	public String main(Model model, @AuthenticationPrincipal AccountUserDetails user, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		//	週と日を格納する二次元配列を用意する
		List<List<LocalDate>> month = new ArrayList<>();
		
		// 1週間分のLocalDateを格納するListを用意する
		List<LocalDate> week = new ArrayList<>();
		
		//日にちを格納する変数を用意する
		LocalDate day, start, end;
		
		//今月 or 前月 or 翌月を判断
		if (date == null) {
			//その月の1日を取得する
			day = LocalDate.now(); //現在日時を取得
			day = LocalDate.of(day.getYear(), day.getMonthValue(), 1);
		//現在日時からその月の１日を取得
		} else {
			day = date; //引数で受け取った日付をそのまま使う
		}
		
		//カレンダーのToDo直下に「yyyy年mm月」と表示
		model.addAttribute("month", day.format(DateTimeFormatter.ofPattern("yyyy年MM月")));
		
		//前月のリンク
		model.addAttribute("prev", day.minusMonths(1));
		
		//翌月のリンク
		model.addAttribute("next", day.plusMonths(1));
		
		//前月分のLocalDateを求める
		DayOfWeek w = day.getDayOfWeek(); //当該日の曜日を取得
		if (w != DayOfWeek.SUNDAY) {
			day = day.minusDays(w.getValue()); //1日からマイナス
		}
		start = day;
		
		//1週目（1日ずつ増やして 週のリストに格納していく）
		for(int i = 1; i <= 7; i++) {
			week.add(day); //週のリストへ格納
			day = day.plusDays(1); //1日進める
		}
		month.add(week); //週のリストを新しく作る
		
		week = new ArrayList<>(); //次週のリストを新しく作る
		
		//2週目（「7」から始めているのは2週目だから）
		int leftOfMonth = day.lengthOfMonth() - day.getDayOfMonth();
		leftOfMonth = day.lengthOfMonth() - leftOfMonth;
		leftOfMonth = 7 - leftOfMonth;
		
		for(int i = 7; i <= day.lengthOfMonth() + leftOfMonth; i++) {
			week.add(day); //週のリストへ格納
			
			w = day.getDayOfWeek();
			if(w == DayOfWeek.SATURDAY) { //土曜日だったら
				month.add(week); //当該週リストを、月のリストへ格納する
				week = new ArrayList<>(); //次週のリストを新しく作る
			}
			
			day = day.plusDays(1); //1日進める
		}
		
		//最終週の翌日分
		DayOfWeek endofmonth = day.getDayOfWeek();
		int next = 7 - endofmonth.getValue();
		if (next == 0) {
			next = 7;
		}
		
		for(int n = 1; n <= next; n++) {
			week.add(day);
			day = day.plusDays(1);
		}
		month.add(week);
		
		end = day;
		
		//日付とタスクを紐づけるコレクション
		MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();
		
		List<Tasks> list;
		
		//管理者だったら
		if(user.getUsername().equals("admin")) {
			list = repo.findAllByDateBetween(start.atTime(0, 0), end.atTime(0, 0));
		} else { //ユーザーだったら
			list = repo.findByDateBetween(start.atTime(0, 0), end.atTime(0, 0), user.getName());
		}
		
		// 取得したタスクをコレクションに追加
		for (Tasks task : list) {
			tasks.add(task.getDate().toLocalDate(), task);
		}
		
		//カレンダーのデータをHTMLに連携
		model.addAttribute("matrix", month);
		
		//コレクションのデータをHTMLに連携
		model.addAttribute("tasks", tasks);
		
		//HTML表示
		return "main";
	}
	
	//タスク登録用の表示画面
	@GetMapping("/main/create/{date}")
	public String create(Model model, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		return "create";
	}
	
	//タスク登録用
	@PostMapping("/main/create")
	public String ceratePost(Model model, TaskForm form, @AuthenticationPrincipal AccountUserDetails user) {
		Tasks task = new Tasks();
		task.setName(user.getName());
		task.setTitle(form.getTitle());
		task.setText(form.getText());
		task.setDate(form.getDate().atTime(0, 0));
		
		repo.save(task);
		
		return "redirect:/main";
	}
	
	//タスク編集画面表示用
	@GetMapping("/main/edit/{id}")
	public String edit(Model model, @PathVariable Integer id) {
		Tasks task = repo.getById(id);
		model.addAttribute("task", task);
		return "edit";
	}
	
	//タスク編集用
	@PostMapping("/main/edit/{id}")
	public String editPost(Model model, TaskForm form, @PathVariable Integer id, @AuthenticationPrincipal AccountUserDetails user) {
		Tasks task = new Tasks();
		task.setId(id);
		
		task.setName(user.getName());
		task.setTitle(form.getTitle());
		task.setText(form.getText());
		task.setDate(form.getDate().atTime(0, 0));
		task.setDone(form.isDone());
		
		repo.save(task);
		
		return "redirect:/main";
	}
	
	//タスク削除用
	@PostMapping("/main/delete/{id}")
	public String deletePost(Model model, TaskForm form, @PathVariable Integer id) {
		Tasks task = new Tasks();
		task.setId(id);
		
		repo.delete(task);
		
		return "redirect:/main";
	}

}
