package trash;

import com.pxs.reaper.Constant;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class TimestampFormatterAdapter extends XmlAdapter<String, Timestamp> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);

    @Override
    public String marshal(final Timestamp date) throws Exception {
        return dateFormat.format(date);
    }

    @Override
    public Timestamp unmarshal(final String date) throws Exception {
        return new Timestamp(dateFormat.parse(date).getTime());
    }

}
