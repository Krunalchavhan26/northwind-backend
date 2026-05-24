package com.northwind.dao;

import java.util.Optional;

import com.northwind.entity.User;
import com.northwind.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class UserDAO {

	public User save(User user) {
		EntityManager em = JPAUtil.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(user);
			tx.commit();
			return user;
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	public Optional<User> findByClerkUserId(String clerkUserId) {
		EntityManager em = JPAUtil.getEntityManager();
		try {
			return em.createQuery("SELECT u FROM User u WHERE u.clerkUserId = :id", User.class)
					.setParameter("id", clerkUserId).getResultStream().findFirst();
		} finally {
			em.close();
		}
	}

	public User update(User user) {
		EntityManager em = JPAUtil.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			User updated = em.merge(user);
			tx.commit();
			return updated;
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	public void deleteByClerkUserId(String clerkUserId) {
		EntityManager em = JPAUtil.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.createQuery("DELETE FROM User u WHERE u.clerkUserId = :id").setParameter("id", clerkUserId)
					.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	public Optional<User> findByEmail(String email) {
		EntityManager em = JPAUtil.getEntityManager();
		try {
			return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
					.setParameter("email", email).getResultStream().findFirst();
		} finally {
			em.close();
		}
	}
}