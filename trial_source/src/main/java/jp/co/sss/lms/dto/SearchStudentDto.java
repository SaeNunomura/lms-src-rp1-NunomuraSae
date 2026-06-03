package jp.co.sss.lms.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 受講生一覧検索結果DTO
 *
 * @author 布村沙英 -Task.57
 */
@Component
@Data
public class SearchStudentDto {

	/**LMSユーザーID */
	private Integer lmsUserId;
	/**ユーザーID */
	private Integer userId;
	/**ユーザーネ名*/
	private String userName;
	/**コースID */
	private Integer courseId;
	/**コース名 */
	private String courseName;
	/**会社名 */
	private String companyName;
	/**会場名 */
	private String placeName;
}
