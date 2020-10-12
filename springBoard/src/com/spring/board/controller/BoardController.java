package com.spring.board.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.board.HomeController;
import com.spring.board.service.boardService;
import com.spring.board.vo.BoardVo;
import com.spring.board.vo.CodeVo;
import com.spring.board.vo.PageVo;
import com.spring.board.vo.UserVo;
import com.spring.common.CommonUtil;

@Controller
public class BoardController {
	
	@Autowired 
	boardService boardService;
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);	
	
	
	@RequestMapping(value = "/board/boardList.do", method = RequestMethod.GET)
	public String boardList(Locale locale, Model model,PageVo pageVo,HttpServletRequest request,HttpSession session) 
			throws Exception{
		String userId = (String)session.getAttribute("userId");
		System.out.println("세션 저장된 ID : "  + userId);
		List<BoardVo> boardList = new ArrayList<BoardVo>(); //글 가져올 List 생성
		List<CodeVo> codeList = new ArrayList<CodeVo>(); //타입 가져올 List 생성
		HashMap<String, Object> map = new HashMap<String, Object>(); //page와 checkbox 선택지 용
		HashMap<String, String> codeMap = new HashMap<String, String>(); //타입 id - name 짝 map 생성
		
		int page = 1; //페이지 초기화
		int totalCnt = 0; //totalCnt 초기화
		
		if(pageVo.getPageNo() == 0){
			pageVo.setPageNo(page);;
		}
		
		map.put("pageVo",pageVo); //pageVo put
		

		codeList = boardService.selectCode("menu");//타입id, 타입 이름 가져오기	
		for(int i=0; i<4;i++) {
			codeMap.put(codeList.get(i).getCodeId(),codeList.get(i).getCodeName());
		}//타입id, 타입 이름List Map에 담기
		
		
		String[] boardType; //checkbox 선택지 가져올 배열 생성
		boardType=request.getParameterValues("boardType"); //checkbox 선택지 가져오기
		map.put("boardType", boardType);//checkbox 선택지 map에 담기 
		
		
		boardList = boardService.selectBoardType(map);//pageVo와 checkbox 선택지 넘김
		
		totalCnt = boardService.boardTypeListCount(boardType); //checkbox 선택지 넘김 
		 
		String userName=""; //로그인한 ID로 이름 가져오기
		if(userId !=null) {
			userName = boardService.selectName(userId);
			session.setAttribute("userName", userName);
		}else {
			session.setAttribute("userName", "SYSTEM");
		}
		
		
		model.addAttribute("boardList", boardList);
		model.addAttribute("totalCnt", totalCnt);
		model.addAttribute("pageNo", page);
		model.addAttribute("codeList",codeList);
		model.addAttribute("codeMap",codeMap);
		model.addAttribute("userId",userId);
		model.addAttribute("userName", userName);
		
		return "board/boardList";
	}
	//글 상세 페이지
	@RequestMapping(value = "/board/{boardType}/{boardNum}/boardView.do", method = RequestMethod.GET)
	public String boardView(Locale locale, Model model
			,@PathVariable("boardType")String boardType
			,@PathVariable("boardNum")int boardNum) throws Exception{
		
		BoardVo boardVo = new BoardVo();
		
		List<CodeVo> codeList = new ArrayList<CodeVo>();

		codeList = boardService.selectCode("menu");
		HashMap<String, String> codeMap = new HashMap<String, String>();
		for(int i=0; i<4;i++) {
			codeMap.put(codeList.get(i).getCodeId(),codeList.get(i).getCodeName());
		}
		
		boardVo = boardService.selectBoard(boardType,boardNum);
		
		String userId= boardVo.getCreator();
		String userName;
		if(userId!="SYSTEM") {
			userName = boardService.selectName(userId);
		}else {
			userName="SYSTEM";
		}
		
		
		model.addAttribute("boardType", boardType);
		model.addAttribute("boardNum", boardNum);
		model.addAttribute("board", boardVo);
		model.addAttribute("codeMap",codeMap);
		model.addAttribute("userName",userName);
		
		return "board/boardView";
	}
	
	//글쓰기 폼 호출
	@RequestMapping(value = "/board/boardWrite.do", method = RequestMethod.GET)
	public String boardWrite(Locale locale, Model model,HttpServletRequest request,HttpSession session) throws Exception{
		List<CodeVo> codeList= new ArrayList<CodeVo>();
		codeList = boardService.selectCode("menu");
		
		String userName = (String)session.getAttribute("userName");
		String userId = (String)session.getAttribute("userId");
		
		
		model.addAttribute("codeList",codeList);
		model.addAttribute("userName",userName);
		model.addAttribute("userId",userId);
		return "board/boardWrite";
	}
	
	//글등록 수행
	@RequestMapping(value = "/board/boardWriteAction.do", method = RequestMethod.POST)
	@ResponseBody
	public String boardWriteAction(Locale locale, BoardVo boardVo) throws Exception{
	
		
		HashMap<String, String> result = new HashMap<String, String>();
		CommonUtil commonUtil = new CommonUtil();
		int resultCnt = 0;
		
		System.out.println(boardVo.getBoardVoList().get(0).getBoardType());
		System.out.println(boardVo.getBoardVoList().get(0).getBoardTitle());
		System.out.println(boardVo.getBoardVoList().get(0).getCreator());
		
		
		resultCnt=boardService.boardInsert(boardVo);
		System.out.println("result Count : " + resultCnt);
		/*StringTokenizer Type = new StringTokenizer(boardVo.getBoardType(),",");
		StringTokenizer Title = new StringTokenizer(boardVo.getBoardTitle(),",");
		StringTokenizer Comment = new StringTokenizer(boardVo.getBoardComment(),",");
		
		while(Type.hasMoreTokens())
		{
		
			
			boardVo.setBoardType(Type.nextToken());
			boardVo.setBoardTitle(Title.nextToken());
			boardVo.setBoardComment(Comment.nextToken());
			
			resultCnt = boardService.boardInsert(boardVo);
		}*/
		
	
		/*result.put("success", (resultCnt > 0)?"Y":"N");
		String callbackMsg = commonUtil.getJsonCallBackString(" ",result);
		
		System.out.println("callbackMsg::"+callbackMsg);*/
		
		String resultmsg="";
		if (resultCnt>0) {
			resultmsg="<script>alert('SUCCESS');location.href='/board/boardList.do'</script>";
		}else {
			resultmsg="<script>alert('FAIL');location.href='/board/boardWrite.do'</script>";

		}
		return resultmsg;
		
	}
	
	//글 수정페이지 호출
	@RequestMapping(value="/board/boardModify.do", method=RequestMethod.GET)
	public String boardModifyView(BoardVo boardVo,Model model)throws Exception {
		
		boardVo=boardService.boardModifyView(boardVo);
		
		List<CodeVo> codeList = new ArrayList<CodeVo>();
		codeList = boardService.selectCode("menu");
		HashMap<String, String> codeMap = new HashMap<String, String>();
		for(int i=0; i<4;i++) {
			codeMap.put(codeList.get(i).getCodeId(),codeList.get(i).getCodeName());
		}
		
		String userId= boardVo.getCreator();
		String userName;
		if(userId!="SYSTEM") {
			userName = boardService.selectName(userId);
		}else {
			userName="SYSTEM";
		}
		
		model.addAttribute("board", boardVo);
		model.addAttribute("codeMap",codeMap);
		model.addAttribute("userName",userName);
		
		return "board/boardModify";
	}
	
	
	//수정완료 누른 후
	@RequestMapping(value="/board/boardModifyAction.do", method=RequestMethod.POST)
	@ResponseBody
	public String boardModify(BoardVo boardVo,Model model)throws Exception {
		HashMap<String, String> result = new HashMap<String, String>();
		CommonUtil commonUtil = new CommonUtil();
	
		int resultCnt = boardService.boardUpdate(boardVo);

		
		result.put("success", (resultCnt > 0)?"Y":"N");
		
		String callbackMsg = commonUtil.getJsonCallBackString(" ",result);
		
		System.out.println("callbackMsg::"+callbackMsg);
		
		return callbackMsg;
	}
	
	//글삭제
	@RequestMapping(value="/board/boardDelete.do", method=RequestMethod.POST)
	@ResponseBody
	public String boardDelete(BoardVo boardVo)throws Exception {
		HashMap<String, String> result = new HashMap<String, String>();
		CommonUtil commonUtil = new CommonUtil();
		System.out.println("삭제시  넘기는 VO num : " + boardVo.getBoardNum()+ "\n");
		System.out.println("삭제시  넘기는 VO Type : " + boardVo.getBoardType()+ "\n");
		
		int resultCnt=boardService.boardDelete(boardVo);
		
		result.put("success", (resultCnt > 0)?"Y":"N");
		
		String callbackMsg = commonUtil.getJsonCallBackString(" ",result);
		
		System.out.println("callbackMsg::"+callbackMsg);
		
		return callbackMsg;
	}
	
	//회원가입
	@RequestMapping(value="/board/boardJoin.do", method=RequestMethod.GET)
	public String boardJoin(Model model, UserVo userVo)throws Exception{
		List<CodeVo> codeList = new ArrayList<CodeVo>();
		codeList = boardService.selectCode("phone");
		
		model.addAttribute("codeList",codeList);
		
		
		return "board/boardJoin";
	}

	//Id 중복체크
	@RequestMapping(value="board/checkId.do", method=RequestMethod.POST)
	@ResponseBody
	public String checkId(String userId)throws Exception{
		System.out.println("넘어온 Id : " + userId);
		
		String checkId=boardService.selectId(userId);
		String result;
		if(checkId==null) { //아이디 없어서 사용가능한경우 
			result="{\"result\" : \"idAvailable\"}";
		}else { //아이디 있어서 사용 불가능한 경우 
			result="{\"result\" : \"idNotAvailable\"}";
		}
		
		System.out.println(result);
		return result;
	}
	
	//회원가입 처리
	@RequestMapping(value="/board/boardJoinAction.do", method=RequestMethod.POST, 
			produces = "application/text; charset=UTF-8")
	@ResponseBody
	public String boardJoinAction(Model model, UserVo userVo)throws Exception{
		System.out.println("userId : " + userVo.getUserId());
		System.out.println("userName : " + userVo.getUserName());
		
		String result;		
		int insertUser = boardService.insertUser(userVo);
		
		System.out.println("insertUsert : " + insertUser);
		if(insertUser>0) {
			result = "{\"result\" : \"회원가입 성공\"}";
		}else {
			result = "{\"result\" : \"회원가입 실패\"}";
		}
		System.out.println("result : " + result);
		return result;
	}
	
	
	//로그인 폼 호출
	@RequestMapping(value="/board/LoginForm.do", method=RequestMethod.GET)
	public String LoginForm(Model model){
		
		return "board/boardLoginForm";
	}
	
	//로그인  액션
	@RequestMapping(value="/board/LoginAction.do", method=RequestMethod.POST, produces = "application/text; charset=UTF-8")
	@ResponseBody
	public String LoginAction(Model model,String userId, String userPw,HttpServletRequest request,HttpSession session) 
			throws Exception{
		System.out.println("전달받은 id : " + userId);
		System.out.println("전달받은 pw : " + userPw);
		
		String result;
		
		if(boardService.selectId(userId)==null) {
			System.out.println("userId null 진입");
			result="{\"result\" : \"존재하지 않는 ID\"}";
		}else if(boardService.selectPw(userPw)==null){
			System.out.println("password null 진입");
			result="{\"result\" : \"비밀번호가 틀렸습니다\"}";
		}else {
			System.out.println("login success 진입");
			result="{\"result\" : \"로그인 성공\"}";
			session.setAttribute("userId", userId);
			}				

		return result;
	}
	
	
	//로그아웃 액션
	@RequestMapping(value="/board/Logout.do")
	public String Logout(Model model, HttpServletRequest request, HttpSession session)
	throws Exception{
		session.invalidate();
		
		return "redirect:/board/boardList.do";
	}
	
	
}