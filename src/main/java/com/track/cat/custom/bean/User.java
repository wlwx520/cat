package com.track.cat.custom.bean;

import java.util.List;

import com.track.cat.persistent.PersistentBean;
import com.track.cat.persistent.annotation.Column;
import com.track.cat.persistent.annotation.Persistent;
import com.track.cat.persistent.annotation.PrimaryKeyAutoincrement;
import com.track.cat.persistent.annotation.SimpleRelation;

@Persistent(table = "user")
public class User extends PersistentBean {
	@Column
	private String name;
	@PrimaryKeyAutoincrement
	private int age;
	@Column
	private long idCardNum;
	@Column
	private boolean flg;
	@Column
	private char ch;
	@Column
	private short sh;
	@Column
	private double d;
	@Column
	private float f;
	@SimpleRelation
	private List<Role> roles;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public long getIdCardNum() {
		return idCardNum;
	}

	public void setIdCardNum(long idCardNum) {
		this.idCardNum = idCardNum;
	}

	public boolean isFlg() {
		return flg;
	}

	public void setFlg(boolean flg) {
		this.flg = flg;
	}

	public char getCh() {
		return ch;
	}

	public void setCh(char ch) {
		this.ch = ch;
	}

	public short getSh() {
		return sh;
	}

	public void setSh(short sh) {
		this.sh = sh;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	public float getF() {
		return f;
	}

	public void setF(float f) {
		this.f = f;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}
