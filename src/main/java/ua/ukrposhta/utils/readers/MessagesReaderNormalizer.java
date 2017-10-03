package ua.ukrposhta.utils.readers;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class MessagesReaderNormalizer extends XmlAdapter<String, String> {
    @Override
    public String marshal(String text) {
        return text.trim();
    }

    @Override
    public String unmarshal(String str) throws Exception {
        if(str.isEmpty()) {
            return str;
        } else {
            int sz = str.length();
            char[] chs = new char[sz];
            int count = 0;

            for(int i = 0; i < sz; ++i) {
                if(str.charAt(i) != ' ') {
                    chs[count++] = str.charAt(i);
                } else if (i > 0 && str.charAt(i-1) != ' ' && str.charAt(i-1) != '\n') {
                    chs[count++] = str.charAt(i);
                }
            }

            if(count == sz) {
                return str;
            } else {
                return new String(chs, 0, count);
            }
        }
    }
}
