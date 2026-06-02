package jp.co.sss.lms.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.co.sss.lms.dto.CourseServiceCourseDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.service.CourseService;

/**
 * コースコントローラー
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/course")
public class CourseController {

	@Autowired
	private CourseService courseService;
	@Autowired
	private LoginUserDto loginUserDto;

	/**
	 * コース詳細画面 初期表示
	 * 
	 * @param model
	 * @return コース詳細画面
	 * @throws ParseException
	 */
	@RequestMapping(path = "/detail")
	public String detail(Model model) throws ParseException {

		// パラメータチェック
		String message = courseService.checkCourseId(loginUserDto.getCourseId());
		if (!message.isEmpty()) {
			return "illegal";
		}

		// コース詳細関連情報の取得
		CourseServiceCourseDto courseServiceCourseDto = courseService
				.getCourseDetail(loginUserDto.getCourseId());
		model.addAttribute("courseServiceCourseDto", courseServiceCourseDto);

		return "course/detail";
	}
	
	@RequestMapping(path = "/list")
	public String list() {
		return "course/list";
	}

}
