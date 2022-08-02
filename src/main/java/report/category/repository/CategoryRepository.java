package report.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import report.category.entity.CategoryEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository <CategoryEntity, Long> {
    Optional<CategoryEntity> findByIdAndDeleteFlag(Long id, String deleteFlag);

    List<CategoryEntity> findByDepthAndDeleteFlag(int depth, String deleteFalg);
}
