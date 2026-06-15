package jp.co.sss.lms.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 布村沙英 -Task.58
 * 勤怠一括登録フォーム
 */
@Data
public class BulkRegistForm {
	/**会場ID*/
	private Integer placeId;
	/**会場名*/
	private String placeName;
	/**期間(開始)*/
	@NotBlank
	private String searchPeriodFrom;
	/**期間(終了)*/
	@NotBlank
	private String searchPeriodTo;
	/**期間(開始～終了)*/
	private String searchPeriod;
	/**日別勤怠フォームリスト*/
	private List<DailyAttendanceForm> dailyAttendanceFormList;
}
