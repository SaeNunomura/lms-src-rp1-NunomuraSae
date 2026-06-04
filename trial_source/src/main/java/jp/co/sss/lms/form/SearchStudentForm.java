package jp.co.sss.lms.form;

import java.util.LinkedHashMap;

import lombok.Data;

/**
 * 受講生検索フォーム 布村沙英 -Task.57
 */
@Data
public class SearchStudentForm {
	
	/**LMSユーザーID*/
	private Integer lmsUserId;
	/**コースID*/
	private Integer courseId;
	/**コース名*/
	private String courseName;
	/**会場ID*/
	private Integer placeId;
	/**会場名*/
	private String placeName;
	/**企業ID*/
	private Integer companyId;
	/**企業名*/
	private String companyName;
	/**ユーザー名*/
	private String userName;
	/**セレクトリスト用コースマップ*/
	private LinkedHashMap<Integer, String> courseMap;
	/**セレクトリスト用会場マップ*/
	private LinkedHashMap<Integer, String> placeMap;
	/**セレクトリスト用企業マップ*/
	private LinkedHashMap<Integer, String> companyMap;
}
