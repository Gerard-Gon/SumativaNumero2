package TechStoreChile.TechStoreChile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import TechStoreChile.TechStoreChile.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
