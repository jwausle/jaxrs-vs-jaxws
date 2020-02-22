package schnittstelle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = PojoList.POJOS)
public class PojoList {
    static final String POJOS = "pojos";
    static final String POJO = "pojo";

    private final List<Pojo> pojos = new ArrayList<>();

    public PojoList() {/* MUST for @XmlRootElement */}

    @JsonCreator
    public PojoList(@JsonProperty(POJO) Collection<Pojo> pojos) {
        if (pojos != null) {
            this.pojos.addAll(pojos);
        }
    }

    @XmlElement(name = POJO)
    @JsonGetter(POJOS)
    public List<Pojo> getPojos() {
        return this.pojos;
    }

    public void setPojos(List<Pojo> newList) {
        pojos.clear();
        pojos.addAll(newList);
    }
}
