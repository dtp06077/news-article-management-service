package ch10;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Servlet implementation class NewsController
 */
@WebServlet(urlPatterns = "/news.nhn")
@MultipartConfig(maxFileSize = 1024*1024*2 , location = "c:/Temp/img")

public class NewsController extends HttpServlet{
    private static final long serialVersionUID =1L;
    
    private NewsDAO dao;
    private ServletContext ctx;
    
    private final String START_PAGE = "ch10/newsList.jsp";
    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        dao = new NewsDAO();
        ctx = getServletContext();
    }       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewsController() {
        super();
        // TODO Auto-generated constructor stub
    }
    private String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */
    
    protected void service(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
          request.setCharacterEncoding("utf-8");
          String action =request.getParameter("action");

          dao = new NewsDAO();

          Method m;
          String view ;
          
          if(action == null){
              action = "listNews";
          }
          
          if ("listNews".equals(action)) {
              view = listNews(request);
          } else if("getNews".equals(action)) {
             view= getNews(request);
 
          } else if ("addNews".equals(action)) {
              view = addNews(request);
          } else if ("deleteNews".equals(action)) {
              view = deleteNews(request);
          } else {
              view = START_PAGE; 
          }


          if(view.startsWith("redirect:/")){
              String rview = view.substring("redirect:/".length());
              response.sendRedirect(rview);
          }else{
              RequestDispatcher dispatcher = request.getRequestDispatcher(view);
              dispatcher.forward(request, response);
          }
    }
    
    public String addNews(HttpServletRequest request){
        News n = new News();
        try{
            Part part = request.getPart("file");
            String fileName = getFilename(part);
            if(fileName != null && !fileName.isEmpty()){
                part.write(fileName);
            }
            BeanUtils.populate(n,request.getParameterMap());
            n.setImg("/img/"+fileName);
            dao.addNews(n);
        }catch(Exception e){
            e.printStackTrace();
            ctx.log("뉴스 추가 과정에서 문제 발생!!");
            request.setAttribute("error","뉴스가 정상적으로 등록되지 않았습니다!!");
            return listNews(request);
        }
        return "redirect:/news.nhn?action=listNews";
    }
    
    public String listNews(HttpServletRequest request){
        List<News> list;
        try{
            list = dao.getAll();
            request.setAttribute("newslist",list);

        } catch(Exception e){
            e.printStackTrace();
            ctx.log("뉴스 목록 생성 과정에서 문제 발생!!");
            request.setAttribute("error","뉴스 목록이 정상적으로 처리되지 않았습니다!!");

        }
        return "ch10/newsList.jsp";

    }
    
    public String getNews(HttpServletRequest request){
        int aid = Integer.parseInt(request.getParameter("aid"));
        try{
            News n = dao.getNews(aid);
            request.setAttribute("news", n);
        }catch (SQLException e){
            e.printStackTrace();
            ctx.log("뉴스를 가져오는 과정에서 문제발생!!");
            request.setAttribute("error","뉴스를 정상적으로 가져오지 못했습니다!!");

        }
        return "ch10/newsView.jsp";

    }

    public String deleteNews(HttpServletRequest request){
        int aid = Integer.parseInt(request.getParameter("aid"));
        try{
            dao.delNews(aid);
        }catch(SQLException e){
            e.printStackTrace();
            ctx.log("뉴스 삭제 과정에서 문제 발생!!");
            request.setAttribute("error","뉴스가 정상적으로 삭제되지 않았습니다!!");
            return listNews(request);
        }
        return "redirect:/news.nhn?action=listNews" ;

    }
    
    
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
      response.getWriter().append("Served at: ").append(request.getContextPath());
   }

   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
      doGet(request, response);
   }

}
