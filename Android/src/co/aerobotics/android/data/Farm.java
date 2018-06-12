package co.aerobotics.android.data;

/**
 * Created by michaelwootton on 6/12/18.
 */

public class Farm {
    private String name;
    private Integer id;

    public Farm(String name, Integer id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
