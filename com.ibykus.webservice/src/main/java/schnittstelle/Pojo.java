package schnittstelle;

import java.security.SecureRandom;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement // that's different to JAX-RS Pojo.class.
public final class Pojo {
    static final String JSON_ID = "id";
    static final String JSON_VALUE = "value";

    private String id;
    private Integer value;

    public Pojo() {/* MUST exist because @XmlRootElement */}


    @XmlElement(name = JSON_ID)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = JSON_VALUE)
    public Integer getValue() {
        return this.value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("{id='%s', value=%s}", this.id, this.value);
    }

    /**
     * Generate+set a random ID. Overwrite existing one.
     */
    String generateAndSetId() {
        this.id = new SecureRandom().ints(48, 122)
                .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
                .mapToObj(i -> (char) i)
                .limit(10)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pojo pojo = (Pojo) o;
        return Objects.equals(id, pojo.id) &&
                Objects.equals(value, pojo.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }
}
