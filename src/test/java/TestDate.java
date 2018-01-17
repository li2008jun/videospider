import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TestDate {
	
	public static void main(String[] args) throws ParseException {
		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	    Long time=new Long(1504452054063l);  
	    String d = format.format(time);  
	    Date date=format.parse(d);  
	    System.out.println("Format To Date:"+date);  
	}

}
