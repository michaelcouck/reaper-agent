package trash;

import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.couchbase.client.java.repository.annotation.Field;
import com.couchbase.client.java.repository.annotation.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.couchbase.core.mapping.Document;

import javax.persistence.Column;
import java.util.Date;

@Getter
@Setter
@Document
public class Persistable {

    @Id
    protected String id;

    @Field
    private String type;

    @Column
    @JsonIgnoreProperties(ignoreUnknown = true)
    private Date date;

}