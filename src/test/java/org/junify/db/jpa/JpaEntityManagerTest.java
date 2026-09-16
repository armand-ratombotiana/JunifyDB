package org.junify.db.jpa;

import jakarta.persistence.*;
import org.junify.db.JunifyDB;
import org.junify.db.config.JunifyDBConfig;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JPA EntityManager Specification Integration Tests")
class JpaEntityManagerTest {

    private JunifyDB db;
    private EntityManagerFactory emf;
    private EntityManager em;

    @BeforeEach
    void setUp() {
        db = JunifyDB.create(JunifyDB.embed()
                .storageEngine(JunifyDBConfig.StorageEngineType.IN_MEMORY)
                .buildConfig());
        emf = JunifyPersistence.createEntityManagerFactory(db);
        em = emf.createEntityManager();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (em != null && em.isOpen()) em.close();
        if (emf != null && emf.isOpen()) emf.close();
        if (db != null) db.close();
    }

    @Entity
    @Table(name = "employees")
    static class Employee {
        @Id
        @GeneratedValue
        private String id;

        @Column(name = "emp_name")
        private String name;

        private String department;
        private double salary;

        public Employee() {}

        public Employee(String name, String department, double salary) {
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDepartment() { return department; }
        public double getSalary() { return salary; }
        public void setSalary(double salary) { this.salary = salary; }
    }

    @Test
    @DisplayName("JPA: Standard persist, find, merge, remove")
    void testBasicLifecycle() {
        Employee emp = new Employee("Alice Smith", "Engineering", 120000.0);
        assertNull(emp.getId());

        em.persist(emp);
        assertNotNull(emp.getId(), "persist() should populate @GeneratedValue id");

        // find
        Employee found = em.find(Employee.class, emp.getId());
        assertNotNull(found);
        assertEquals("Alice Smith", found.getName());
        assertEquals("Engineering", found.getDepartment());
        assertTrue(em.contains(found));

        // merge
        found.setSalary(135000.0);
        em.merge(found);

        Employee updated = em.find(Employee.class, emp.getId());
        assertEquals(135000.0, updated.getSalary(), 0.001);

        // remove
        em.remove(updated);
        assertNull(em.find(Employee.class, emp.getId()), "Entity must be deleted after remove()");
    }

    @Test
    @DisplayName("JPA: EntityTransaction commit and rollback")
    void testTransactionCommitAndRollback() {
        EntityTransaction tx = em.getTransaction();
        assertFalse(tx.isActive());

        // Commit flow
        tx.begin();
        assertTrue(tx.isActive());
        Employee emp1 = new Employee("Bob Jones", "Marketing", 85000.0);
        em.persist(emp1);
        tx.commit();
        assertFalse(tx.isActive());

        assertNotNull(em.find(Employee.class, emp1.getId()));

        // Rollback flow
        tx.begin();
        Employee emp2 = new Employee("Charlie Brown", "Finance", 95000.0);
        em.persist(emp2);
        tx.rollback();
        assertFalse(tx.isActive());

        assertNull(em.find(Employee.class, emp2.getId()), "Rollback must revert uncommitted persist");
    }

    @Test
    @DisplayName("JPA: createQuery (JPQL / SQL) with parameters and pagination")
    void testCreateQuery() {
        em.persist(new Employee("Dev 1", "Tech", 70000.0));
        em.persist(new Employee("Dev 2", "Tech", 90000.0));
        em.persist(new Employee("Dev 3", "Tech", 110000.0));
        em.persist(new Employee("Sales 1", "Sales", 60000.0));

        // Named parameter query
        TypedQuery<Employee> query = em.createQuery(
                "SELECT * FROM employees WHERE department = :dept AND salary >= :minSal ORDER BY salary ASC",
                Employee.class);
        query.setParameter("dept", "Tech");
        query.setParameter("minSal", 80000.0);

        List<Employee> results = query.getResultList();
        assertEquals(2, results.size());
        assertEquals("Dev 2", results.get(0).getName());
        assertEquals("Dev 3", results.get(1).getName());

        // Pagination: setMaxResults & setFirstResult
        TypedQuery<Employee> pagedQuery = em.createQuery(
                "SELECT * FROM employees WHERE department = 'Tech' ORDER BY salary DESC",
                Employee.class);
        pagedQuery.setFirstResult(1);
        pagedQuery.setMaxResults(1);

        Employee single = pagedQuery.getSingleResult();
        assertEquals("Dev 2", single.getName());
    }

    @Test
    @DisplayName("JPA: createNativeQuery update execution")
    void testNativeQueryExecuteUpdate() {
        Employee emp = new Employee("Original", "Support", 50000.0);
        em.persist(emp);

        int updatedCount = em.createNativeQuery("UPDATE employees SET salary = ? WHERE id = ?")
                .setParameter(1, 55000.0)
                .setParameter(2, emp.getId())
                .executeUpdate();

        assertEquals(1, updatedCount);

        Employee reloaded = em.find(Employee.class, emp.getId());
        assertEquals(55000.0, reloaded.getSalary(), 0.001);
    }
}
