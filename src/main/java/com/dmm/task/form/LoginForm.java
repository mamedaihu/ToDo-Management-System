package com.dmm.task.form;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class LoginForm {
	
	/**
	 * ID
	 */
	@Max(value = 99999)
	@NotNull
	private Integer userId;
	
	/**
	 * パスワード
	 */
	@NotBlank
	private String userPass;
	
	

}
