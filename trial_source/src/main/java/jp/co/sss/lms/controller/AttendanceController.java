package jp.co.sss.lms.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.validation.Valid;
import jp.co.sss.lms.dto.AttendanceManagementDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.dto.SearchStudentDto;
import jp.co.sss.lms.form.AttendanceForm;
import jp.co.sss.lms.form.BulkRegistForm;
import jp.co.sss.lms.form.DailyAttendanceForm;
import jp.co.sss.lms.form.SearchStudentForm;
import jp.co.sss.lms.service.StudentAttendanceService;
import jp.co.sss.lms.util.AttendanceUtil;
import jp.co.sss.lms.util.Constants;

/**
 * 勤怠管理コントローラ
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/attendance")
public class AttendanceController {

	@Autowired
	private StudentAttendanceService studentAttendanceService;
	@Autowired
	private LoginUserDto loginUserDto;
	@Autowired
	private AttendanceUtil attendanceUtil;

	/**
	 * 勤怠管理画面 初期表示
	 * 
	 * @param lmsUserId
	 * @param courseId
	 * @param model
	 * @return 勤怠管理画面
	 * @throws ParseException
	 */
	@RequestMapping(path = "/detail", method = RequestMethod.GET)
	public String index(Model model) {

		// 勤怠一覧の取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);

		//現在より過去に未入力が無いかチェック 布村沙英 -Task.25
		model.addAttribute("notEnterCheck", studentAttendanceService.notEnterCheck());

		return "attendance/detail";
	}

	/**
	 * 勤怠管理画面 『出勤』ボタン押下
	 * 
	 * @param model
	 * @return 勤怠管理画面
	 */
	@RequestMapping(path = "/detail", params = "punchIn", method = RequestMethod.POST)
	public String punchIn(Model model) {

		// 更新前のチェック
		String error = studentAttendanceService.punchCheck(Constants.CODE_VAL_ATWORK);
		model.addAttribute("error", error);
		// 勤怠登録
		if (error == null) {
			String message = studentAttendanceService.setPunchIn();
			model.addAttribute("message", message);
		}
		// 一覧の再取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);

		return "attendance/detail";
	}

	/**
	 * 勤怠管理画面 『退勤』ボタン押下
	 * 
	 * @param model
	 * @return 勤怠管理画面
	 */
	@RequestMapping(path = "/detail", params = "punchOut", method = RequestMethod.POST)
	public String punchOut(Model model) {

		// 更新前のチェック
		String error = studentAttendanceService.punchCheck(Constants.CODE_VAL_LEAVING);
		model.addAttribute("error", error);
		// 勤怠登録
		if (error == null) {
			String message = studentAttendanceService.setPunchOut();
			model.addAttribute("message", message);
		}
		// 一覧の再取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);

		return "attendance/detail";
	}

	/**
	 * 勤怠管理画面 『勤怠情報を直接編集する』リンク押下
	 * 
	 * @param model
	 * @return 勤怠情報直接変更画面
	 */
	@RequestMapping(path = "/update")
	public String update(Model model) {

		// 勤怠管理リストの取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		// 勤怠フォームの生成
		AttendanceForm attendanceForm = studentAttendanceService
				.setAttendanceForm(attendanceManagementDtoList);
		model.addAttribute("attendanceForm", attendanceForm);

		return "attendance/update";
	}

	/**
	 * 勤怠情報直接変更画面 『更新』ボタン押下
	 * 
	 * @param attendanceForm
	 * @param model
	 * @param result
	 * @return 勤怠管理画面
	 * @throws ParseException
	 */
	@RequestMapping(path = "/update", params = "complete", method = RequestMethod.POST)
	public String complete(@ModelAttribute AttendanceForm attendanceForm, Model model, BindingResult result)
			throws ParseException {

		//入力チェック 布村沙英 -Task.27
		studentAttendanceService.updateInputCheck(attendanceForm, result);
		//エラーが起きた場合、マップを取得し勤怠情報直接入力画面に遷移 布村沙英 -Task.27
		if (result.hasErrors()) {
			//中抜け時間の選択肢用マップを取得
			attendanceForm.setBlankTimes(attendanceUtil.setBlankTime());
			//出勤時間(時間)選択肢用の時間マップを取得 布村沙英 -Task.26
			attendanceForm.setTrainingStartHourMap(attendanceUtil.getHourMap());
			//出勤時間(分)選択肢用の時間マップを取得 布村沙英 -Task.26
			attendanceForm.setTrainingStartMinMap(attendanceUtil.getMinMap());
			//退勤時間(時間)選択肢用の分マップを取得 布村沙英 -Task.26
			attendanceForm.setTrainingEndHourMap(attendanceUtil.getHourMap());
			//退勤時間(分)選択肢用の分マップを取得 布村沙英 -Task.26
			attendanceForm.setTrainingEndMinMap(attendanceUtil.getMinMap());
			return "attendance/update";
		}
		// 更新
		studentAttendanceService.formatConversion(attendanceForm);
		String message = studentAttendanceService.update(attendanceForm);
		model.addAttribute("message", message);
		// 一覧の再取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);

		return "attendance/detail";
	}

	/**
	 * 
	 * 「研修管理」→「勤怠確認」押下
	 * 
	 * @author 布村沙英 -Task.57
	 * @param model
	 * @return 勤怠情報確認（受講生一覧）
	 */
	@RequestMapping(path = "list")
	public String list(Model model) {
		//ログインしている者の会場IDを元に、受講生検索フォームを取得
		SearchStudentForm searchStudentForm = studentAttendanceService.getSearchStudentForm(loginUserDto.getPlaceId());
		model.addAttribute("searchStudentForm", searchStudentForm);
		return "attendance/list";
	}

	/**
	 * 勤怠情報確認（受講生一覧）画面『検索』ボタン押下
	 * 
	 * @author 布村沙英
	 * @param searchStudentForm
	 * @param model
	 * @return 勤怠情報確認（受講生一覧）
	 */
	@RequestMapping(path = "list", params = "search")
	public String search(SearchStudentForm searchStudentForm, Model model) {
		//送信されたフォームを元に受講生検索メソッドを実行、検索結果をリストに格納
		List<SearchStudentDto> searchStudentDtoList = studentAttendanceService.searchStudent(searchStudentForm);
		model.addAttribute("searchStudentDtoList", searchStudentDtoList);
		//検索結果の受講生ごとの過去日勤怠未入力チェック
		List<Boolean> searchStudentNotEnterCheckList = studentAttendanceService
				.searchStudentNotEnterCheck(searchStudentDtoList);
		model.addAttribute("searchStudentNotEnterCheckList", searchStudentNotEnterCheckList);
		//ログインしている者の会場IDを元に、受講生検索フォームを取得
		model.addAttribute("searchStudentForm",
				studentAttendanceService.getSearchStudentForm(loginUserDto.getPlaceId()));
		return "attendance/list";
	}

	/**
	 * 勤怠情報確認（受講生一覧）『勤怠確認』ボタン押下
	 * 
	 * @author 布村沙英 -Task.57
	 * @param searchStudentForm
	 * @param model
	 * @return 勤怠管理画面
	 */
	@RequestMapping(path = "detail", params = "attendance_updateActionForm", method = RequestMethod.POST)
	public String attendanceUpdateActionForm(SearchStudentForm searchStudentForm, Model model) {
		// 勤怠一覧の取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(searchStudentForm.getCourseId(), searchStudentForm.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);
		model.addAttribute("searchStudentForm", searchStudentForm);
		return "attendance/detail";
	}

	/**
	 * 「研修管理」→「勤怠一括登録」押下
	 * @author 布村沙英 -Task.58
	 * @param bulkRegistForm
	 * @param model
	 * @return 勤怠一括登録画面
	 */
	@RequestMapping(path = "bulkRegist", method = RequestMethod.GET)
	public String bulkRegist(BulkRegistForm bulkRegistForm, Model model) {
		studentAttendanceService.setBulkRegistForm(bulkRegistForm);
		model.addAttribute("bulkRegistForm", bulkRegistForm);
		return "attendance/bulkRegist";
	}

	/**
	 * 勤怠一括登録画面『検索』ボタン押下
	 * 
	 * @author 布村沙英 -Task.58
	 * @param bulkRegistForm
	 * @param result
	 * @param model
	 * @return勤怠一括登録画面
	 */
	@RequestMapping(path = "bulkRegist/search", params = "search", method = RequestMethod.POST)
	public String bulkRegistSearch(@Valid @ModelAttribute BulkRegistForm bulkRegistForm, BindingResult result,
			Model model) {
		//検索フォームの入力チェック
		studentAttendanceService.searchInputCheck(bulkRegistForm, result);
		if (result.hasErrors()) {
			return "attendance/bulkRegist";
		}
		//検索結果から日別受講生勤怠情報リストを取得、スコープに保存
		List<DailyAttendanceForm> dailyAttendanceFormList = studentAttendanceService.getUserAttendance(bulkRegistForm);
		model.addAttribute("dailyAttendanceFormList", dailyAttendanceFormList);
		//初期表示処理
		studentAttendanceService.setBulkRegistForm(bulkRegistForm);
		model.addAttribute("bulkRegistForm", bulkRegistForm);
		return "attendance/bulkRegist";
	}

	@RequestMapping(path = "bulkRegist/complete", params = "complete", method = RequestMethod.POST)
	public String bulkRegistComplete(@ModelAttribute BulkRegistForm bulkRegistForm, Model model, BindingResult result) {
		studentAttendanceService.bulkRegistInputCheck(bulkRegistForm, result);
		if (result.hasErrors()) {
			//検索結果から日別受講生勤怠情報リストを取得、スコープに保存
			List<DailyAttendanceForm> dailyAttendanceFormList = studentAttendanceService
					.getUserAttendance(bulkRegistForm);
			model.addAttribute("dailyAttendanceFormList", dailyAttendanceFormList);
			//初期表示処理
			studentAttendanceService.setBulkRegistForm(bulkRegistForm);
			model.addAttribute("bulkRegistForm", bulkRegistForm);
			return "attendance/bulkRegist";
		}
		//初期表示のための仮コード
		studentAttendanceService.setBulkRegistForm(bulkRegistForm);
		model.addAttribute("bulkRegistForm", bulkRegistForm);
		return "attendance/bulkRegist";
	}

}