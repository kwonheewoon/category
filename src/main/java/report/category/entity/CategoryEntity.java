package report.category.entity;

import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import report.category.dto.CategoryDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class CategoryEntity extends BaseEntity {

    @Id @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    @Column(name = "category_nm", nullable = false, length = 255)
    private String categoryNm;

    @Column(name = "depth", nullable = false, length = 255)
    private int depth;

    @Column(name = "order_no", nullable = false, length = 255)
    private int orderNo;

    @Column(name = "delete_flag", nullable = false, length = 1)
    @ColumnDefault("'N'")
    private String deleteFlag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    @Builder.Default
    private List<CategoryEntity> childCategoryList = new ArrayList<>();


    public void setParent(CategoryEntity parentCategory){
        this.parentCategory = parentCategory;
    }

    public void changeDepth(int depth){
        this.depth = depth;
    }

    public void changeOrderNo(int orderNo){
        this.orderNo = orderNo;
    }

    public void changeCategoryNm(String categoryNm){
        if(null != categoryNm && !categoryNm.isEmpty()){
            this.categoryNm = categoryNm;
        }
    }

    public void changeParentCategory(CategoryEntity parentCategory){
        this.parentCategory = parentCategory;
    }

    public void addChildCategory(CategoryEntity parentCategory){
        parentCategory.setParent(this);
        this.childCategoryList.add(parentCategory);
    }

    @PrePersist
    public void setField(){
        this.depth = (this.depth > 0) ? this.depth : 1;
        this.deleteFlag = (null == this.deleteFlag || this.deleteFlag.isEmpty()) ? "N":this.deleteFlag;
    }

    public static CategoryEntity entityConvert(CategoryDto categoryDto){
        return CategoryEntity.builder()
                .id(categoryDto.getId())
                .build();
    }

}
