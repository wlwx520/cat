package com.track.cat.custom.bean;

import java.util.List;

import com.track.cat.persistent.PersistentBean;
import com.track.cat.persistent.annotation.Column;
import com.track.cat.persistent.annotation.ComplexRelation;
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

	@ComplexRelation("complexUsers")
	private List<Role> complexRoles;
}
