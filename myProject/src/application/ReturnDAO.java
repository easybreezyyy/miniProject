package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import controllers.AdminController;
import controllers.DeliverController;

public class ReturnDAO {

	StringBuffer sql = new StringBuffer();
	ResultSet rs = null;
	Connection con = null;
	PreparedStatement pstmt = null;
	
	/** 상품 주문시 리턴리스트에 데이터 추가 */
	public int insertData(ReturnVO rt) {
		int i = 0;
		sql.setLength(0);
		sql.append("insert into returnlist values(return_seq.nextval, ?,?,?,?,?,?,?,?,?)");
				//(seq, id, stylenum, rentaldate, returndate, name, phone, address, status, rentalnum)
		try {
			con = ConnUtil.getConnection();
			pstmt = con.prepareStatement(sql.toString());
			pstmt.setString(1, rt.getId());
			pstmt.setString(2, rt.getStylenum());
			pstmt.setDate(3, new java.sql.Date(rt.getRentaldate().getTime()));
			pstmt.setDate(4, new java.sql.Date(rt.getReturndate().getTime()));
			pstmt.setString(5, rt.getName());
			pstmt.setString(6, rt.getPhone());
			pstmt.setString(7, rt.getAddress());
			pstmt.setString(8, rt.getStatus());
			pstmt.setInt(9, rt.getRentalnum());
			i = pstmt.executeUpdate();
			System.out.println(i + "행 DB insert 성공");
			
		}catch(SQLException e) {
			System.err.println("쿼리문 에러 - DB insert 실패");
			e.printStackTrace();
		}finally { ConnUtil.closeAll(con, pstmt, rs);}
		return i;
	}
	
	
	/** 금일 수거 목록 조회 메서드 */
	public void getReturnTable() {
		RecentTableVO rt = null;
		sql.setLength(0);
		sql.append("select rentalnum, returnnum, to_char(returndate, 'RRRR/MM/DD') as returndate, "
				+ "name, id, stylenum, status, address, phone from returnlist " 
				+ "where trunc(returndate) = trunc(sysdate)");
		try {
			con = application.ConnUtil.getConnection();
			pstmt = con.prepareStatement(sql.toString());
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				rt = new RecentTableVO();
				
				rt.setRentalnum(rs.getInt("rentalnum"));
				rt.setReturndate(rs.getString("returndate"));
				rt.setAddress(rs.getString("address"));
				rt.setStatus(rs.getString("status"));
				rt.setStylenum(rs.getString("stylenum"));
				rt.setReturnnum(rs.getInt("returnnum")); 
				rt.setId(rs.getString("id"));
				rt.setName(rs.getString("name"));
				rt.setPhone(rs.getString("phone"));
				
				AdminController.recentList.add(rt);
			}
		}catch (SQLException e) {
			System.out.println("테이블 연동 실패");
			e.printStackTrace();
		}finally {application.ConnUtil.closeAll(con, pstmt, rs);}
	}
	
	/** 금일 연체 목록 조회 메서드 */
	public void getOverdueTable() {
		RecentTableVO rt = null;
		sql.setLength(0);
		sql.append("select rentalnum, address, status, stylenum, returnnum, id, name, phone, "
				+ "to_char(returndate, 'RRRR/MM/DD') as returndate, "
				+ "to_char(rentaldate, 'RRRR/MM/DD') as rentaldate from returnlist "
				+ "where returndate < trunc(sysdate) "
				+ "order by returndate asc");
		try {
			con = application.ConnUtil.getConnection();
			pstmt = con.prepareStatement(sql.toString());
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				rt = new RecentTableVO();
				
				rt.setRentalnum(rs.getInt("rentalnum"));
				rt.setAddress(rs.getString("address"));
				rt.setStatus(rs.getString("status"));
				rt.setStylenum(rs.getString("stylenum"));
				rt.setReturnnum(rs.getInt("returnnum"));
				rt.setId(rs.getString("id"));
				rt.setName(rs.getString("name"));
				rt.setReturndate(rs.getString("returndate"));
				rt.setRentaldate(rs.getString("rentaldate"));
				rt.setPhone(rs.getString("phone"));
				
				AdminController.recentList.add(rt);
			}
		}catch (SQLException e) {
			System.out.println("테이블 연동 실패");
			e.printStackTrace();
		}finally {application.ConnUtil.closeAll(con, pstmt, rs);}
	}
	
	/** 테이블 세팅 메서드 
	 * Deliver - Collect 메뉴 클릭시
	 * 예상 반납일이 오래된 순으로 (연체가 오래된 순으로) 정렬
	 */
	public void getCollectTable(String state) {
		CollectTableVO ct = null;
		sql.setLength(0);
		sql.append("select name,phone,address,stylenum,rentalnum,returnnum from returnlist "
				+ "where status = '미수거' and address like '%' || ? || '%' and returndate <= trunc(sysdate) "
				+ "order by returndate asc");
		try {
			con = application.ConnUtil.getConnection();
			pstmt = con.prepareStatement(sql.toString());
			pstmt.setString(1, state);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				ct = new CollectTableVO();
				
				ct.setAddress(rs.getString("address"));
				ct.setName(rs.getString("name"));
				ct.setPhone(rs.getString("phone"));
				ct.setStylenum(rs.getString("stylenum"));
				ct.setRentalnum(rs.getInt("rentalnum"));
				ct.setReturnnum(rs.getInt("returnnum"));
				
				//System.out.println(ct);
				DeliverController.collectList.add(ct);
			}
		} catch (SQLException e) {
			System.out.println("테이블 연동 실패");
			e.printStackTrace();
		} finally {
			application.ConnUtil.closeAll(con, pstmt, rs);
		}
	}
	
	
	/** Deliver - Collect - Complete 버튼 눌렀을 때
	 * 반납 완료시 반납 테이블에서 데이터 삭제
	 */
	public int deleteData(int rentalnum) {
		int i = 0;
		sql.setLength(0);
		sql.append("delete from returnlist where rentalnum = ?");
		try {
			con = application.ConnUtil.getConnection();
			pstmt=con.prepareStatement(sql.toString());
			pstmt.setInt(1, rentalnum);
			i = pstmt.executeUpdate();
			System.out.println(i + "행이 삭제되었습니다.");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {application.ConnUtil.closeAll(con, pstmt, rs);}
		
		return i;
		
	}
	
}
