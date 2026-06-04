package jp.co.sss.lms.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import jp.co.sss.lms.dto.AttendanceManagementDto;
import jp.co.sss.lms.dto.CompanyDto;
import jp.co.sss.lms.dto.CourseDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.dto.PlaceDto;
import jp.co.sss.lms.dto.SearchStudentDto;
import jp.co.sss.lms.entity.TStudentAttendance;
import jp.co.sss.lms.enums.AttendanceStatusEnum;
import jp.co.sss.lms.form.AttendanceForm;
import jp.co.sss.lms.form.DailyAttendanceForm;
import jp.co.sss.lms.form.SearchStudentForm;
import jp.co.sss.lms.mapper.TSearchStudentMapper;
import jp.co.sss.lms.mapper.TStudentAttendanceMapper;
import jp.co.sss.lms.util.AttendanceUtil;
import jp.co.sss.lms.util.Constants;
import jp.co.sss.lms.util.DateUtil;
import jp.co.sss.lms.util.LoginUserUtil;
import jp.co.sss.lms.util.MessageUtil;
import jp.co.sss.lms.util.TrainingTime;

/**
 * 勤怠情報（受講生入力）サービス
 * 
 * @author 東京ITスクール
 */
@Service
public class StudentAttendanceService {

	@Autowired
	private DateUtil dateUtil;
	@Autowired
	private AttendanceUtil attendanceUtil;
	@Autowired
	private MessageUtil messageUtil;
	@Autowired
	private LoginUserUtil loginUserUtil;
	@Autowired
	private LoginUserDto loginUserDto;
	@Autowired
	private TStudentAttendanceMapper tStudentAttendanceMapper;
	@Autowired
	private TSearchStudentMapper tSearchStudentMapper;

	/**
	 * 勤怠一覧情報取得
	 * 
	 * @param courseId
	 * @param lmsUserId
	 * @return 勤怠管理画面用DTOリスト
	 */
	public List<AttendanceManagementDto> getAttendanceManagement(Integer courseId,
			Integer lmsUserId) {

		// 勤怠管理リストの取得
		List<AttendanceManagementDto> attendanceManagementDtoList = tStudentAttendanceMapper
				.getAttendanceManagement(courseId, lmsUserId, Constants.DB_FLG_FALSE);
		for (AttendanceManagementDto dto : attendanceManagementDtoList) {
			// 中抜け時間を設定
			if (dto.getBlankTime() != null) {
				TrainingTime blankTime = attendanceUtil.calcBlankTime(dto.getBlankTime());
				dto.setBlankTimeValue(String.valueOf(blankTime));
			}
			// 遅刻早退区分判定
			AttendanceStatusEnum statusEnum = AttendanceStatusEnum.getEnum(dto.getStatus());
			if (statusEnum != null) {
				dto.setStatusDispName(statusEnum.name);
			}
		}

		return attendanceManagementDtoList;
	}

	/**
	 * 出退勤更新前のチェック
	 * 
	 * @param attendanceType
	 * @return エラーメッセージ
	 */
	public String punchCheck(Short attendanceType) {
		Date trainingDate = attendanceUtil.getTrainingDate();
		// 権限チェック
		if (!loginUserUtil.isStudent()) {
			return messageUtil.getMessage(Constants.VALID_KEY_AUTHORIZATION);
		}
		// 研修日チェック
		if (!attendanceUtil.isWorkDay(loginUserDto.getCourseId(), trainingDate)) {
			return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_NOTWORKDAY);
		}
		// 登録情報チェック
		TStudentAttendance tStudentAttendance = tStudentAttendanceMapper
				.findByLmsUserIdAndTrainingDate(loginUserDto.getLmsUserId(), trainingDate,
						Constants.DB_FLG_FALSE);
		switch (attendanceType) {
		case Constants.CODE_VAL_ATWORK:
			if (tStudentAttendance != null
					&& !tStudentAttendance.getTrainingStartTime().equals("")) {
				// 本日の勤怠情報は既に入力されています。直接編集してください。
				return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_PUNCHALREADYEXISTS);
			}
			break;
		case Constants.CODE_VAL_LEAVING:
			if (tStudentAttendance == null
					|| tStudentAttendance.getTrainingStartTime().equals("")) {
				// 出勤情報がないため退勤情報を入力出来ません。
				return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_PUNCHINEMPTY);
			}
			if (!tStudentAttendance.getTrainingEndTime().equals("")) {
				// 本日の勤怠情報は既に入力されています。直接編集してください。
				return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_PUNCHALREADYEXISTS);
			}
			TrainingTime trainingStartTime = new TrainingTime(
					tStudentAttendance.getTrainingStartTime());
			TrainingTime trainingEndTime = new TrainingTime();
			if (trainingStartTime.compareTo(trainingEndTime) > 0) {
				// 退勤時刻は出勤時刻より後でなければいけません。
				return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_TRAININGTIMERANGE);
			}
			break;
		}
		return null;
	}

	/**
	 * 出勤ボタン処理
	 * 
	 * @return 完了メッセージ
	 */
	public String setPunchIn() {
		// 当日日付
		Date date = new Date();
		// 本日の研修日
		Date trainingDate = attendanceUtil.getTrainingDate();
		// 現在の研修時刻
		TrainingTime trainingStartTime = new TrainingTime();
		// 遅刻早退ステータス
		AttendanceStatusEnum attendanceStatusEnum = attendanceUtil.getStatus(trainingStartTime,
				null);
		// 研修日の勤怠情報取得
		TStudentAttendance tStudentAttendance = tStudentAttendanceMapper
				.findByLmsUserIdAndTrainingDate(loginUserDto.getLmsUserId(), trainingDate,
						Constants.DB_FLG_FALSE);
		if (tStudentAttendance == null) {
			// 登録処理
			tStudentAttendance = new TStudentAttendance();
			tStudentAttendance.setLmsUserId(loginUserDto.getLmsUserId());
			tStudentAttendance.setTrainingDate(trainingDate);
			tStudentAttendance.setTrainingStartTime(trainingStartTime.toString());
			tStudentAttendance.setTrainingEndTime("");
			tStudentAttendance.setStatus(attendanceStatusEnum.code);
			tStudentAttendance.setNote("");
			tStudentAttendance.setAccountId(loginUserDto.getAccountId());
			tStudentAttendance.setDeleteFlg(Constants.DB_FLG_FALSE);
			tStudentAttendance.setFirstCreateUser(loginUserDto.getLmsUserId());
			tStudentAttendance.setFirstCreateDate(date);
			tStudentAttendance.setLastModifiedUser(loginUserDto.getLmsUserId());
			tStudentAttendance.setLastModifiedDate(date);
			tStudentAttendance.setBlankTime(null);
			tStudentAttendanceMapper.insert(tStudentAttendance);
		} else {
			// 更新処理
			tStudentAttendance.setTrainingStartTime(trainingStartTime.toString());
			tStudentAttendance.setStatus(attendanceStatusEnum.code);
			tStudentAttendance.setDeleteFlg(Constants.DB_FLG_FALSE);
			tStudentAttendance.setLastModifiedUser(loginUserDto.getLmsUserId());
			tStudentAttendance.setLastModifiedDate(date);
			tStudentAttendanceMapper.update(tStudentAttendance);
		}
		// 完了メッセージ
		return messageUtil.getMessage(Constants.PROP_KEY_ATTENDANCE_UPDATE_NOTICE);
	}

	/**
	 * 退勤ボタン処理
	 * 
	 * @return 完了メッセージ
	 */
	public String setPunchOut() {
		// 当日日付
		Date date = new Date();
		// 本日の研修日
		Date trainingDate = attendanceUtil.getTrainingDate();
		// 研修日の勤怠情報取得
		TStudentAttendance tStudentAttendance = tStudentAttendanceMapper
				.findByLmsUserIdAndTrainingDate(loginUserDto.getLmsUserId(), trainingDate,
						Constants.DB_FLG_FALSE);
		// 出退勤時刻
		TrainingTime trainingStartTime = new TrainingTime(
				tStudentAttendance.getTrainingStartTime());
		TrainingTime trainingEndTime = new TrainingTime();
		// 遅刻早退ステータス
		AttendanceStatusEnum attendanceStatusEnum = attendanceUtil.getStatus(trainingStartTime,
				trainingEndTime);
		// 更新処理
		tStudentAttendance.setTrainingEndTime(trainingEndTime.toString());
		tStudentAttendance.setStatus(attendanceStatusEnum.code);
		tStudentAttendance.setDeleteFlg(Constants.DB_FLG_FALSE);
		tStudentAttendance.setLastModifiedUser(loginUserDto.getLmsUserId());
		tStudentAttendance.setLastModifiedDate(date);
		tStudentAttendanceMapper.update(tStudentAttendance);
		// 完了メッセージ
		return messageUtil.getMessage(Constants.PROP_KEY_ATTENDANCE_UPDATE_NOTICE);
	}

	/**
	 * 勤怠フォームへ設定
	 * 
	 * @param attendanceManagementDtoList
	 * @return 勤怠編集フォーム
	 */
	public AttendanceForm setAttendanceForm(
			List<AttendanceManagementDto> attendanceManagementDtoList) {

		AttendanceForm attendanceForm = new AttendanceForm();
		attendanceForm.setAttendanceList(new ArrayList<DailyAttendanceForm>());
		attendanceForm.setLmsUserId(loginUserDto.getLmsUserId());
		attendanceForm.setUserName(loginUserDto.getUserName());
		attendanceForm.setLeaveFlg(loginUserDto.getLeaveFlg());
		attendanceForm.setBlankTimes(attendanceUtil.setBlankTime());
		//出勤時間(時間)選択肢用の時間マップを取得 布村沙英 -Task.26
		attendanceForm.setTrainingStartHourMap(attendanceUtil.getHourMap());
		//出勤時間(分)選択肢用の時間マップを取得 布村沙英 -Task.26
		attendanceForm.setTrainingStartMinMap(attendanceUtil.getMinMap());
		//退勤時間(時間)選択肢用の分マップを取得 布村沙英 -Task.26
		attendanceForm.setTrainingEndHourMap(attendanceUtil.getHourMap());
		//退勤時間(分)選択肢用の分マップを取得 布村沙英 -Task.26
		attendanceForm.setTrainingEndMinMap(attendanceUtil.getMinMap());

		// 途中退校している場合のみ設定
		if (loginUserDto.getLeaveDate() != null) {
			attendanceForm
					.setLeaveDate(dateUtil.dateToString(loginUserDto.getLeaveDate(), "yyyy-MM-dd"));
			attendanceForm.setDispLeaveDate(
					dateUtil.dateToString(loginUserDto.getLeaveDate(), "yyyy年M月d日"));
		}

		// 勤怠管理リストの件数分、日次の勤怠フォームに移し替え
		for (AttendanceManagementDto attendanceManagementDto : attendanceManagementDtoList) {
			DailyAttendanceForm dailyAttendanceForm = new DailyAttendanceForm();
			dailyAttendanceForm
					.setStudentAttendanceId(attendanceManagementDto.getStudentAttendanceId());
			dailyAttendanceForm
					.setTrainingDate(dateUtil.toString(attendanceManagementDto.getTrainingDate()));
			dailyAttendanceForm
					.setTrainingStartTime(attendanceManagementDto.getTrainingStartTime());

			dailyAttendanceForm.setTrainingEndTime(attendanceManagementDto.getTrainingEndTime());
			if (attendanceManagementDto.getBlankTime() != null) {
				dailyAttendanceForm.setBlankTime(attendanceManagementDto.getBlankTime());
				dailyAttendanceForm.setBlankTimeValue(String.valueOf(
						attendanceUtil.calcBlankTime(attendanceManagementDto.getBlankTime())));
			}

			//出勤時刻を「時」「分」に分割してセットし、表示用の日付文字列を生成してセットする 布村沙英 -Task.26
			String startTime = attendanceManagementDto.getTrainingStartTime();
			dailyAttendanceForm.setTrainingStartTimeHour(attendanceUtil.getStartHour(startTime));
			dailyAttendanceForm.setTrainingStartTimeMin(attendanceUtil.getStartMin(startTime));
			//退勤時刻を「時」「分」に分割してセットし、表示用の日付文字列を生成してセットする 布村沙英 -Task.26
			String endTime = attendanceManagementDto.getTrainingEndTime();
			dailyAttendanceForm.setTrainingEndTimeHour(attendanceUtil.getEndHour(endTime));
			dailyAttendanceForm.setTrainingEndTimeMin(attendanceUtil.getEndMin(endTime));

			dailyAttendanceForm.setStatus(String.valueOf(attendanceManagementDto.getStatus()));
			dailyAttendanceForm.setNote(attendanceManagementDto.getNote());
			dailyAttendanceForm.setSectionName(attendanceManagementDto.getSectionName());
			dailyAttendanceForm.setIsToday(attendanceManagementDto.getIsToday());
			dailyAttendanceForm.setDispTrainingDate(dateUtil
					.dateToString(attendanceManagementDto.getTrainingDate(), "yyyy年M月d日(E)"));
			dailyAttendanceForm.setStatusDispName(attendanceManagementDto.getStatusDispName());

			attendanceForm.getAttendanceList().add(dailyAttendanceForm);
		}

		return attendanceForm;
	}

	/**
	 * 勤怠登録・更新処理
	 * 
	 * @param attendanceForm
	 * @return 完了メッセージ
	 * @throws ParseException
	 */
	public String update(AttendanceForm attendanceForm) throws ParseException {

		Integer lmsUserId = loginUserUtil.isStudent() ? loginUserDto.getLmsUserId()
				: attendanceForm.getLmsUserId();

		// 現在の勤怠情報（受講生入力）リストを取得
		List<TStudentAttendance> tStudentAttendanceList = tStudentAttendanceMapper
				.findByLmsUserId(lmsUserId, Constants.DB_FLG_FALSE);

		// 入力された情報を更新用のエンティティに移し替え
		Date date = new Date();
		for (DailyAttendanceForm dailyAttendanceForm : attendanceForm.getAttendanceList()) {

			// 更新用エンティティ作成
			TStudentAttendance tStudentAttendance = new TStudentAttendance();
			// 日次勤怠フォームから更新用のエンティティにコピー
			BeanUtils.copyProperties(dailyAttendanceForm, tStudentAttendance);
			// 研修日付
			tStudentAttendance
					.setTrainingDate(dateUtil.parse(dailyAttendanceForm.getTrainingDate()));
			// 現在の勤怠情報リストのうち、研修日が同じものを更新用エンティティで上書き
			for (TStudentAttendance entity : tStudentAttendanceList) {
				if (entity.getTrainingDate().equals(tStudentAttendance.getTrainingDate())) {
					tStudentAttendance = entity;
					break;
				}
			}
			tStudentAttendance.setLmsUserId(lmsUserId);
			tStudentAttendance.setAccountId(loginUserDto.getAccountId());
			// 出勤時間(整形済み) 布村沙英 -Task.26
			tStudentAttendance.setTrainingStartTime(dailyAttendanceForm.getTrainingStartTime());
			// 退勤時間(整形済み) 布村沙英 -Task.26
			tStudentAttendance.setTrainingEndTime(dailyAttendanceForm.getTrainingEndTime());
			// 中抜け時間
			tStudentAttendance.setBlankTime(dailyAttendanceForm.getBlankTime());
			// 遅刻早退ステータス
			TrainingTime trainingStartTime = new TrainingTime(dailyAttendanceForm.getTrainingStartTime());
			TrainingTime trainingEndTime = new TrainingTime(dailyAttendanceForm.getTrainingEndTime());
			;
			if ((trainingStartTime != null || trainingEndTime != null)
					&& !dailyAttendanceForm.getStatusDispName().equals("欠席")) {
				AttendanceStatusEnum attendanceStatusEnum = attendanceUtil
						.getStatus(trainingStartTime, trainingEndTime);
				tStudentAttendance.setStatus(attendanceStatusEnum.code);
			}
			// 備考
			tStudentAttendance.setNote(dailyAttendanceForm.getNote());
			// 更新者と更新日時
			tStudentAttendance.setLastModifiedUser(loginUserDto.getLmsUserId());
			tStudentAttendance.setLastModifiedDate(date);
			// 削除フラグ
			tStudentAttendance.setDeleteFlg(Constants.DB_FLG_FALSE);
			// 登録用Listへ追加
			tStudentAttendanceList.add(tStudentAttendance);
		}
		// 登録・更新処理
		for (TStudentAttendance tStudentAttendance : tStudentAttendanceList) {
			if (tStudentAttendance.getStudentAttendanceId() == null) {
				tStudentAttendance.setFirstCreateUser(loginUserDto.getLmsUserId());
				tStudentAttendance.setFirstCreateDate(date);
				tStudentAttendanceMapper.insert(tStudentAttendance);
			} else {
				tStudentAttendanceMapper.update(tStudentAttendance);
			}
		}
		// 完了メッセージ
		return messageUtil.getMessage(Constants.PROP_KEY_ATTENDANCE_UPDATE_NOTICE);
	}

	/**
	 * @author 布村沙英 -Task.25
	 * @return [未入力日が0より大きい場合]:true,そうでない場合はfalseを戻す。
	 */
	public Boolean notEnterCheck() {
		//今日の日付を取得
		Date trainingDate = attendanceUtil.getTrainingDate();
		final int NO_DATA = 0;
		//[未入力日が0より大きい場合]:true,そうでない場合はfalseを戻す。
		if (NO_DATA < tStudentAttendanceMapper.notEnterCount(loginUserDto.getLmsUserId(), Constants.DB_FLG_FALSE,
				trainingDate)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 出退勤時間(時・分)をhh:mmに整形
	 * @author 布村沙英 -Task.26
	 * @param attendanceForm
	 */
	public void formatConversion(AttendanceForm attendanceForm) {

		for (DailyAttendanceForm dailyAttendanceForm : attendanceForm.getAttendanceList()) {
			// 出勤時刻整形 布村沙英 -Task.26
			Integer trainingStartTimeHour = dailyAttendanceForm.getTrainingStartTimeHour();
			Integer trainingStartTimeMin = dailyAttendanceForm.getTrainingStartTimeMin();
			TrainingTime trainingStartTime = null;
			// 過去日に未入力があった場合はNULLをsetする 布村沙英 -Task.26
			if (trainingStartTimeHour == null || trainingStartTimeMin == null) {
				dailyAttendanceForm.setTrainingStartTime(null);
			} else {
				trainingStartTime = new TrainingTime(
						String.format("%02d:%02d", trainingStartTimeHour, trainingStartTimeMin));
				dailyAttendanceForm.setTrainingStartTime(trainingStartTime.getFormattedString());
			}
			// 退勤時刻整形 布村沙英 -Task.26
			Integer trainingEndTimeHour = dailyAttendanceForm.getTrainingEndTimeHour();
			Integer trainingEndTimeMin = dailyAttendanceForm.getTrainingEndTimeMin();
			TrainingTime trainingEndTime = null;
			// 過去日に未入力があった場合はNULLをsetする
			if (trainingEndTimeHour == null || trainingEndTimeMin == null) {
				dailyAttendanceForm.setTrainingEndTime(null);
			} else {
				trainingEndTime = new TrainingTime(
						String.format("%02d:%02d", trainingEndTimeHour, trainingEndTimeMin));
				dailyAttendanceForm.setTrainingEndTime(trainingEndTime.getFormattedString());
			}
		}

	}

	/**
	 * 入力チェック
	 * @author 布村沙英 -Task.27
	 * @param attendanceForm
	 * @param result
	 */
	public void updateInputCheck(AttendanceForm attendanceForm, BindingResult result) {

		for (int i = 0; i < attendanceForm.getAttendanceList().size(); i++) {
			DailyAttendanceForm dailyAttendanceForm = attendanceForm.getAttendanceList().get(i);
			//備考欄文字数チェック
			final int MAX_LENGTH = 100;
			if (dailyAttendanceForm.getNote() != null && dailyAttendanceForm.getNote().length() > MAX_LENGTH) {
				final String REMARKS = "備考";
				final String ONEHUNDRED = "100";
				String fieldName = String.format("attendanceList[%d].note", i);
				result.addError(new FieldError(
						result.getObjectName(),
						fieldName,
						messageUtil.getMessage("maxlength", new String[] { REMARKS, ONEHUNDRED })));
			}
			//出勤時間（時）、出勤時間（分）の一方が入力有り、もう一方が入力なしの場合
			if (dailyAttendanceForm.getTrainingStartTimeHour() == null
					^ dailyAttendanceForm.getTrainingStartTimeMin() == null) {
				final String START_TIME = "出勤時間";
				String fieldNameHour = String.format("attendanceList[%d].trainingStartTimeHour", i);
				String fieldNameMin = String.format("attendanceList[%d].trainingStartTimeMin", i);
				//出勤時間（時）が入力なしの場合
				if (dailyAttendanceForm.getTrainingStartTimeHour() == null) {
					result.addError(new FieldError(
							result.getObjectName(),
							fieldNameHour,
							messageUtil.getMessage("input.invalid", new String[] { START_TIME })));
				}
				//出勤時間（分）が入力なしの場合
				if (dailyAttendanceForm.getTrainingStartTimeMin() == null) {
					result.addError(new FieldError(
							result.getObjectName(),
							fieldNameMin,
							messageUtil.getMessage("input.invalid", new String[] { START_TIME })));
				}

			}
			//退勤時間（時）、退勤時間（分）の一方が入力有り、もう一方が入力なしの場合
			if (dailyAttendanceForm.getTrainingEndTimeHour() == null
					^ dailyAttendanceForm.getTrainingEndTimeMin() == null) {
				final String END_TIME = "退勤時間";
				String fieldNameHour = String.format("attendanceList[%d].trainingEndTimeHour", i);
				String fieldNameMin = String.format("attendanceList[%d].trainingEndTimeMin", i);
				//退勤時間（時）が入力なしの場合
				if (dailyAttendanceForm.getTrainingEndTimeHour() == null) {
					result.addError(new FieldError(
							result.getObjectName(),
							fieldNameHour,
							messageUtil.getMessage("input.invalid", new String[] { END_TIME })));
				}
				//退勤時間（分）が入力なしの場合
				if (dailyAttendanceForm.getTrainingEndTimeMin() == null) {
					result.addError(new FieldError(
							result.getObjectName(),
							fieldNameMin,
							messageUtil.getMessage("input.invalid", new String[] { END_TIME })));
				}

			}

			Integer trainingStartTimeHour = dailyAttendanceForm.getTrainingStartTimeHour();
			Integer trainingStartTimeMin = dailyAttendanceForm.getTrainingStartTimeMin();
			Integer trainingEndTimeHour = dailyAttendanceForm.getTrainingEndTimeHour();
			Integer trainingEndTimeMin = dailyAttendanceForm.getTrainingEndTimeMin();
			//出勤時間に入力なし,退勤時間に入力ありの場合
			if (trainingStartTimeHour == null
					&& trainingStartTimeMin == null
					&& trainingEndTimeHour != null
					&& trainingEndTimeMin != null) {
				String fieldName = String.format("attendanceList[%d].trainingStartTime", i);
				result.addError(new FieldError(
						result.getObjectName(),
						fieldName,
						messageUtil.getMessage("attendance.punchInEmpty")));
			}
			//出退勤時間のいずれかが未入力なら、この後の処理をスキップ
			if (trainingStartTimeHour == null || trainingStartTimeMin == null || trainingEndTimeHour == null
					|| trainingEndTimeMin == null) {
				continue;
			}

			String trainingStartTimeString = String.format("%02d%02d", trainingStartTimeHour,
					trainingStartTimeMin);
			Integer trainingStartTimeInteger = Integer.parseInt(trainingStartTimeString);
			String trainingEndTimeString = String.format("%02d%02d", trainingEndTimeHour,
					trainingEndTimeMin);
			Integer trainingEndTimeInteger = Integer.parseInt(trainingEndTimeString);
			//退勤時間より出勤時間が遅い場合
			if (trainingStartTimeInteger > trainingEndTimeInteger) {
				final String INDEX = Integer.toString(i);
				String fieldName = String.format("attendanceList[%d].trainingEndTime", i);
				result.addError(new FieldError(
						result.getObjectName(),
						fieldName,
						messageUtil.getMessage("attendance.trainingTimeRange", new String[] { INDEX })));
			}

			Integer blankTime = dailyAttendanceForm.getBlankTime();
			Integer workingHours = (trainingEndTimeInteger - trainingStartTimeInteger) / 100 * 60;
			//中抜けしていないならこの後の処理をスキップ
			if (blankTime == null) {
				continue;
			}
			//中抜け時間が勤務時間（出勤時間～退勤時間までの時間）を超える場合
			if (blankTime >= workingHours) {
				String fieldName = String.format("attendanceList[%d].blankTime", i);
				result.addError(new FieldError(
						result.getObjectName(),
						fieldName,
						messageUtil.getMessage("attendance.blankTimeError")));
			}
		}
	}

	/**
	 * 受講生検索フォーム取得
	 * @author 布村沙英 -Task.57
	 * @param placeId
	 * @return 受講生検索フォーム
	 */
	public SearchStudentForm getSearchStudentForm(Integer placeId) {
		//コース一覧からセレクトリスト用マップを取得
		List<CourseDto> courseList = tSearchStudentMapper.getCouseList(Constants.DB_HIDDEN_FLG_FALSE,
				Constants.DB_FLG_FALSE);
		LinkedHashMap<Integer, String> courseMap = attendanceUtil.getCourseMap(courseList);
		//会場一覧からセレクトリスト用マップを取得
		List<PlaceDto> placeList = tSearchStudentMapper.getPlaceList(placeId, Constants.DB_FLG_FALSE);
		LinkedHashMap<Integer, String> placeMap = attendanceUtil.getPlaceMap(placeList);
		//企業一覧からセレクトリスト用マップを取得
		List<CompanyDto> companyList = tSearchStudentMapper.getCompanyList(Constants.DB_FLG_FALSE);
		LinkedHashMap<Integer, String> companyMap = attendanceUtil.getCompanyMap(companyList);
		//各マップをフォームクラスに格納
		SearchStudentForm searchStudentForm = new SearchStudentForm();
		searchStudentForm.setCourseMap(courseMap);
		searchStudentForm.setPlaceMap(placeMap);
		searchStudentForm.setCompanyMap(companyMap);

		return searchStudentForm;
	}

	/**
	 *  受講生検索結果を取得
	 * @author 布村沙英 -Task.57
	 * @param searchStudentForm
	 * @return 検索結果DTOリスト
	 */
	public List<SearchStudentDto> searchStudent(SearchStudentForm searchStudentForm) {
		//フォームに入力された値を元に受講生を検索、結果をDTOリストに格納
		List<SearchStudentDto> searchStudentDtoList = new ArrayList<SearchStudentDto>();
		if(searchStudentForm.getCourseId() == 0 && searchStudentForm.getCompanyId() == 0 && searchStudentForm.getUserName() == "") {
			searchStudentDtoList = null;
			return searchStudentDtoList;
		}
		searchStudentDtoList = tSearchStudentMapper.getSearchStudentList(searchStudentForm.getCourseId(),
				searchStudentForm.getCompanyId(), searchStudentForm.getUserName(), searchStudentForm.getPlaceId(),
				Constants.CODE_VAL_ROLL_STUDENT, Constants.DB_FLG_FALSE);
		return searchStudentDtoList;
	}

}
