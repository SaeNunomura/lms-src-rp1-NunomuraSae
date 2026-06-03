package jp.co.sss.lms.form;

import java.util.LinkedHashMap;

import lombok.Data;

@Data
public class SearchStudentForm {

	private LinkedHashMap<Integer, String> courseMap;
	private LinkedHashMap<Integer, String> placeMap;
	private LinkedHashMap<Integer, String> companyMap;
	private String userName;
}
