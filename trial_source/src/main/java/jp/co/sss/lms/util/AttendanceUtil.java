package jp.co.sss.lms.util;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.sss.lms.dto.CompanyDto;
import jp.co.sss.lms.dto.CourseDto;
import jp.co.sss.lms.dto.PlaceDto;
import jp.co.sss.lms.enums.AttendanceStatusEnum;
import jp.co.sss.lms.mapper.MSectionMapper;

/**
 * 勤怠管理のユーティリティクラス
 * 
 * @author 東京ITスクール
 */
@Component
public class AttendanceUtil {

	@Autowired
	private DateUtil dateUtil;
	@Autowired
	private MSectionMapper mSectionMapper;

	/**
	 * SSS定時・出退勤時間を元に、遅刻早退を判定をする
	 * 
	 * @param trainingStartTime 開始時刻
	 * @param trainingEndTime   終了時刻
	 * @return 遅刻早退を判定メソッド
	 */
	public AttendanceStatusEnum getStatus(TrainingTime trainingStartTime,
			TrainingTime trainingEndTime) {
		return getStatus(trainingStartTime, trainingEndTime, Constants.SSS_WORK_START_TIME,
				Constants.SSS_WORK_END_TIME);
	}

	/**
	 * 与えられた定時・出退勤時間を元に、遅刻早退を判定する
	 * 
	 * @param trainingStartTime 開始時刻
	 * @param trainingEndTime   終了時刻
	 * @param workStartTime     定時開始時刻
	 * @param workEndTime       定時終了時刻
	 * @return 判定結果
	 */
	private AttendanceStatusEnum getStatus(TrainingTime trainingStartTime,
			TrainingTime trainingEndTime, TrainingTime workStartTime, TrainingTime workEndTime) {
		// 定時が不明な場合、NONEを返却する
		if (workStartTime == null || workStartTime.isBlank() || workEndTime == null
				|| workEndTime.isBlank()) {
			return AttendanceStatusEnum.NONE;
		}
		boolean isLate = false, isEarly = false;
		// 定時より1分以上遅く出社していたら遅刻(＝はセーフ)
		if (trainingStartTime != null && trainingStartTime.isNotBlank()) {
			isLate = (trainingStartTime.compareTo(workStartTime) > 0);
		}
		// 定時より1分以上早く退社していたら早退(＝はセーフ)
		if (trainingEndTime != null && trainingEndTime.isNotBlank()) {
			isEarly = (trainingEndTime.compareTo(workEndTime) < 0);
		}
		if (isLate && isEarly) {
			return AttendanceStatusEnum.TARDY_AND_LEAVING_EARLY;
		}
		if (isLate) {
			return AttendanceStatusEnum.TARDY;
		}
		if (isEarly) {
			return AttendanceStatusEnum.LEAVING_EARLY;
		}
		return AttendanceStatusEnum.NONE;
	}

	/**
	 * 中抜け時間を時(hour)と分(minute)に変換
	 *
	 * @param min 中抜け時間
	 * @return 時(hour)と分(minute)に変換したクラス
	 */
	public TrainingTime calcBlankTime(int min) {
		int hour = min / 60;
		int minute = min % 60;
		TrainingTime total = new TrainingTime(hour, minute);
		return total;
	}

	/**
	 * 時刻分を丸めた本日日付を取得
	 * 
	 * @return "yyyy/M/d"形式の日付
	 */
	public Date getTrainingDate() {
		Date trainingDate;
		try {
			trainingDate = dateUtil.parse(dateUtil.toString(new Date()));
		} catch (ParseException e) {
			// DateUtil#toStringとparseは同様のフォーマットを使用しているため、起こりえないエラー
			throw new IllegalStateException();
		}
		return trainingDate;
	}

	/**
	 * 休憩時間取得
	 * 
	 * @return 休憩時間
	 */
	public LinkedHashMap<Integer, String> setBlankTime() {
		LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
		map.put(null, "");
		for (int i = 15; i < 480;) {
			int hour = i / 60;
			int minute = i % 60;
			String time;

			if (hour == 0) {
				time = minute + "分";

			} else if (minute == 0) {
				time = hour + "時間";
			} else {
				time = hour + "時" + minute + "分";
			}

			map.put(i, time);

			i = i + 15;

		}
		return map;
	}

	/**
	 * 研修日の判定
	 * 
	 * @param courseId
	 * @param trainingDate
	 * @return 判定結果
	 */
	public boolean isWorkDay(Integer courseId, Date trainingDate) {
		Integer count = mSectionMapper.getSectionCountByCourseId(courseId, trainingDate);
		if (count > 0) {
			return true;
		}
		return false;
	}

	/**
	 * @author 布村沙英 -Task.26
	 * @return 選択肢用の時間マップを取得
	 */
	public LinkedHashMap<Integer, String> getHourMap() {
		LinkedHashMap<Integer, String> hourMap = new LinkedHashMap<>();
		//時間セレクトボックスの中身を取得
		hourMap.put(null, "");
		for (int i = 0; i < 24; i++) {
			hourMap.put(i, String.format("%02d", i));
		}
		return hourMap;
	}

	/**
	 * @author 布村沙英 -Task.26
	 * @return 選択肢用の分マップを取得
	 */
	public LinkedHashMap<Integer, String> getMinMap() {
		LinkedHashMap<Integer, String> minMap = new LinkedHashMap<>();
		//分セレクトボックスの中身を取得
		minMap.put(null, "");
		for (int i = 0; i < 60; i++) {
			minMap.put(i, String.format("%02d", i));
		}
		return minMap;
	}

	/**
	 * @author 布村沙英 -Task.26
	 * @param startTime 出勤時間(hh:mm)
	 * @return 出勤時間の時間を取得
	 */
	public Integer getStartHour(String startTime) {
		if (startTime == null || startTime.length() < 2) {
			return null;
		}

		try {
			return Integer.parseInt(startTime.substring(0, 2));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	* @author 布村沙英 -Task.26
	 * @param startTime 出勤時間(hh:mm)
	 * @return 出勤時間の分を取得
	 */
	public Integer getStartMin(String startTime) {
		if (startTime == null || startTime.length() < 4) {
			return null;
		}

		try {
			return Integer.parseInt(startTime.substring(3, 5));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * @author 布村沙英 -Task.26
	 * @param endTime 退勤時間(hh:mm)
	 * @return 退勤時間の時間を取得
	 */
	public Integer getEndHour(String endTime) {
		if (endTime == null || endTime.length() < 2) {
			return null;
		}

		try {
			return Integer.parseInt(endTime.substring(0, 2));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * @author 布村沙英 -Task.26
	 * @param endTime 退勤時間(hh:mm)
	 * @return 退勤時間の分を取得
	 */
	public Integer getEndMin(String endTime) {
		if (endTime == null || endTime.length() < 4) {
			return null;
		}

		try {
			return Integer.parseInt(endTime.substring(3, 5));
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * コース名のセレクトリスト作成
	 * @param courseList
	 * @return セレクトリスト用のマップを取得
	 */
	public LinkedHashMap<Integer, String> getCourseMap(List<CourseDto> courseList) {
		LinkedHashMap<Integer, String> courseMap = new LinkedHashMap<Integer, String>();
		courseMap.put(null, "");
		for(CourseDto courseDto : courseList) {
			courseMap.put(courseDto.getCourseId(), courseDto.getCourseName());
		}
		return courseMap;
	}
	
	public LinkedHashMap<Integer, String> getPlaceMap(List<PlaceDto> placeList) {
		LinkedHashMap<Integer, String> placeMap = new LinkedHashMap<Integer, String>();
		placeMap.put(null, "");
		for(PlaceDto placeDto : placeList) {
			placeMap.put(placeDto.getPlaceId(), placeDto.getPlaceName());
		}
		return placeMap;
	}
	
	public LinkedHashMap<Integer, String> getCompanyMap(List<CompanyDto> companyList) {
		LinkedHashMap<Integer, String> companyMap = new LinkedHashMap<Integer, String>();
		companyMap.put(null, "");
		for(CompanyDto companyDto : companyList) {
			companyMap.put(companyDto.getCompanyId(), companyDto.getCompanyName());
		}
		return companyMap;
	}

}
