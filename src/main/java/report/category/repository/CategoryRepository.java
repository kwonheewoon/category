package report.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import report.category.entity.CategoryEntity;

@Repository
public interface CategoryRepository extends JpaRepository <CategoryEntity, Long> {

}
