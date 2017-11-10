package trash;

import com.pxs.reaper.Constant;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatterAdapter extends XmlAdapter<String, Date> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);

    @Override
    public String marshal(final Date date) throws Exception {
        return dateFormat.format(date);
    }

    @Override
    public Date unmarshal(final String date) throws Exception {
        return new Date(dateFormat.parse(date).getTime());
    }

}
