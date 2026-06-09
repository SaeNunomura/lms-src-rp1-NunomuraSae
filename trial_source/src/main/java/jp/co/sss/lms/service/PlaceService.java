package jp.co.sss.lms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.lms.entity.MPlace;
import jp.co.sss.lms.mapper.TPlaceIdUserAttendanceMapper;
import jp.co.sss.lms.util.Constants;

@Service
public class PlaceService {
	
	@Autowired
	private TPlaceIdUserAttendanceMapper tPlaceIdUserAttendanceMapper;

	public MPlace findByPlaceId(Integer placeId) {
		MPlace mPlace = new MPlace();
		mPlace = tPlaceIdUserAttendanceMapper.findByPlaceId(placeId, Constants.DB_HIDDEN_FLG_FALSE, Constants.DB_FLG_FALSE);
		return mPlace;
	}
}
