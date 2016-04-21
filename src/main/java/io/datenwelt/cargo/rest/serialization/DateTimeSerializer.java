package io.datenwelt.cargo.rest.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author job
 */
public class DateTimeSerializer extends StdSerializer<DateTime>{

    private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd'T'hh:mm:ss.SSSZ");
    
    public DateTimeSerializer() {
        super(DateTime.class);
    }
    
    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String str = FORMAT.print(value);
        gen.writeString(str);
    }
    
}
