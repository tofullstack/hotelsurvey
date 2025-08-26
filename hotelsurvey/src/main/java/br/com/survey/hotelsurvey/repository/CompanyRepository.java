
package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    Page<Company> findByActiveTrue(Pageable pageable);
    Page<Company> findByActiveFalse(Pageable pageable);

    Page<Company> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
    Page<Company> findByNameContainingIgnoreCaseAndActiveFalse(String name, Pageable pageable);

    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
