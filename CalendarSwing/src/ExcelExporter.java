import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporter {

    /**
     * 월별 거래 내역을 MS Excel (.xlsx) 파일로 출력합니다.
     * ⚠️ 이 코드를 실행하려면 Apache POI 라이브러리가 필요합니다.
     * @param transactions 출력할 거래 내역 리스트
     * @param year 연도
     * @param month 월
     * @param filePath 저장할 파일 경로
     * @return 성공 여부
     */
    public boolean exportMonth(List<Transaction> transactions, int year, int month, String filePath) {
        // 🚨 Apache POI의 Workbook 생성
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet(String.format("%d년 %d월 가계부", year, month));
            
            // 1. 헤더 (Header) 생성
            String[] headers = {"날짜", "구분", "카테고리", "금액", "내용/메모"};
            Row headerRow = sheet.createRow(0);
            
            // 헤더 스타일 설정
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 2. 데이터 (Data) 삽입
            int rowNum = 1;
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);

            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowNum++);
                
                // A열: 날짜
                row.createCell(0).setCellValue(t.getDate()); 
                // B열: 구분 (수입/지출)
                row.createCell(1).setCellValue(t.getType());
                // C열: 카테고리
                row.createCell(2).setCellValue(t.getCategory());
                // D열: 금액 (숫자 형태로 저장, 보기 편하게 포맷팅)
                row.createCell(3).setCellValue(nf.format(t.getAmount()));
                // E열: 내용/메모
                row.createCell(4).setCellValue(t.getContent());
            }

            // 3. 컬럼 폭 자동 조절
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fileOut);
            return true;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "파일 저장 중 오류가 발생했습니다: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}