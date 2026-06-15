package jp.co.sss.lms.form;

import lombok.Data;

/**
 * 日次の勤怠フォーム
 * 
 * @author 東京ITスクール
 */
@Data
public class DailyAttendanceForm {

	/** 受講生勤怠ID */
	private Integer studentAttendanceId;
	/** 途中退校日 */
	private String leaveDate;
	/** 日付 */
	private String trainingDate;
	/** 出勤時間 */
	private String trainingStartTime;
	/** 退勤時間 */
	private String trainingEndTime;
	/** 出勤時間(コピー用)  布村沙英 -Task.58*/
	private String trainingStartTimeCopy;
	/** 退勤時間(コピー用)  布村沙英 -Task.58*/
	private String trainingEndTimeCopy;
	/** 中抜け時間 */
	private Integer blankTime;
	/** 中抜け時間（画面表示用） */
	private String blankTimeValue;
	/** ステータス */
	private String status;
	/** 備考 */
	private String note;
	/** セクション名 */
	private String sectionName;
	/** 当日フラグ */
	private Boolean isToday;
	/** エラーフラグ */
	private Boolean isError;
	/** 日付（画面表示用） */
	private String dispTrainingDate;
	/** ステータス（画面表示用） */
	private String statusDispName;
	/** LMSユーザーID */
	private String lmsUserId;
	/** ユーザー名 */
	private String userName;
	/** コース名 */
	private String courseName;
	/** インデックス */
	private String index;
	/**出勤時間(時間) 布村沙英 -Task.26 */
	private Integer trainingStartTimeHour;
	/**出勤時間(分) 布村沙英  -Task.26*/
	private Integer trainingStartTimeMin;
	/**退勤時間(時間) 布村沙英  -Task.26*/
	private Integer trainingEndTimeHour;
	/**退勤時間(分) 布村沙英 -Task.26*/
	private Integer trainingEndTimeMin;
	/** 企業入力勤怠情報ID 布村沙英 -Task.58*/
	private Integer companyAttendanceId;
	/**欠席フラグ  布村沙英 -Task.58*/
	private Boolean isAbsent;
	/**勤務時間  布村沙英 -Task.58*/
	private Integer trainingTimeRange;
	/**出勤退勤時間フォーマット  布村沙英 -Task.58*/
	private boolean trainingTimeFormat;
}
