package report.category.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class CategoryEntity extends BaseEntity {

    @Id @Column(name = "id", nullable = false)
    @GeneratedValue
    private long id;

    @Column(name = "category_nm", nullable = false, length = 255)
    private String categoryNm;

    @Column(name = "order_no", nullable = false, length = 255)
    private int orderNo;

    @Column(name = "delete_flag", nullable = false, length = 1)
    @ColumnDefault("'N'")
    private String deleteFlag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parentCategory;

    @OneToMany(mappedBy = "parentCategory")
    private List<CategoryEntity> childCategoryList = new ArrayList<>();


    public void setParent(CategoryEntity parentCategory){
        this.parentCategory = parentCategory;
    }

    public void addChildCategory(CategoryEntity parentCategory){
        parentCategory.setParent(this);
        this.childCategoryList.add(parentCategory);
    }
}
