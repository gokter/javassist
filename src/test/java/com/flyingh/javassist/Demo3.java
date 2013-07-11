package com.flyingh.javassist;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import org.junit.Test;

class Point {
	private int x;
	private int y;

	public Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public void move(int dx, int dy) {
		x += dx;
		y += dy;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + "]";
	}

}

class Circle {
	private int x;
	private int y;

	public Circle() {
		super();
	}

	public Circle(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "Circle [x=" + x + ", y=" + y + "]";
	}

}

class Calc {
	public int fact(int n) {
		return n == 1 ? 1 : n * fact(n - 1);
	}
}

class Person {
	private String name;
	private int age;

	public void info() {
		toString();
		name = "flyingh";
		System.out.println(name);
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", age=" + age + "]";
	}

	public void sayHello() {
		System.out.println("hello world!!!");
	}

}

class User {
	private String name = "flycoding";
	private int age = 22;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", age=" + age + "]";
	}

}

public class Demo3 {

	@Test
	public void test6() throws NotFoundException, CannotCompileException,
			IllegalArgumentException, SecurityException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		CtClass ctClass = ClassPool.getDefault().get(
				"com.flyingh.javassist.User");
		CtMethod ctMethod = new CtMethod(CtClass.intType, "getAge",
				new CtClass[] {}, ctClass);
		ctMethod.setBody("{return age;}");
		// ctMethod.setModifiers(ctMethod.getModifiers() & ~Modifier.ABSTRACT);
		ctClass.addMethod(ctMethod);
		Class<?> cls = ctClass.toClass();
		System.out.println(cls.getDeclaredMethod("getAge").invoke(new User()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test5() throws NotFoundException, CannotCompileException,
			IllegalArgumentException, SecurityException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		CtClass ctClass = ClassPool.getDefault().get(
				"com.flyingh.javassist.User");
		// CtMethod ctMethod = CtNewMethod
		// .make("public void say(String name){$0.name=name;System.out.println(\"my name is:\"+this.name);}",
		// ctClass);
		CtMethod ctMethod = CtNewMethod
				.make("public void say(String name){$proceed(name);System.out.println(\"my name is:\"+this.name);}",
						ctClass, "this", "setName");
		ctClass.addMethod(ctMethod);
		ctClass.toClass().getDeclaredMethod("say", String.class)
				.invoke(new User(), "flyingh");
	}

	@Test
	public void test4() throws NotFoundException, CannotCompileException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		CtClass ctClass = ClassPool.getDefault().get(
				"com.flyingh.javassist.User");
		CtMethod ctMethod = CtNewMethod.make(
				"public void say(){System.out.println(name+\",Hello\");}",
				ctClass);
		ctClass.addMethod(ctMethod);
		Class<?> cls = ctClass.toClass();
		Method sayMethod = cls.getDeclaredMethod("say");
		User user = new User();
		sayMethod.invoke(user);
	}

	@Test
	public void test3() throws NotFoundException, CannotCompileException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		CtClass ctClass = ClassPool.getDefault().get(
				"com.flyingh.javassist.Person");
		// CtMethod ctMethod = ctClass.getDeclaredMethod("info");
		CtMethod ctMethod = ctClass.getDeclaredMethod("sayHello");
		ctMethod.instrument(new ExprEditor() {
			@Override
			public void edit(MethodCall m) throws CannotCompileException {
				System.out.println(m.getClassName());
				System.out.println(m.getMethodName());
				if ("com.flyingh.javassist.Person".equals(m.getClassName())
						&& "toString".equals(m.getMethodName())) {
					m.replace("System.out.println(\"abc\");$_=$proceed($$);");
				} else if ("java.io.PrintStream".equals(m.getClassName())
						&& "println".equals(m.getMethodName())) {
					m.replace("System.out.println(\"A\");$_=$proceed($$);System.out.println(\"B\");System.out.println($1);System.out.println($0);System.out.println($class);System.out.println($type);");
				}
			}

			@Override
			public void edit(FieldAccess f) throws CannotCompileException {
				System.out.println(f.getClassName());
				System.out.println(f.getFieldName());
				f.replace("name=\"flycoding\";$_=$proceed($$);");
			}
		});
		Person person = (Person) ctClass.toClass().newInstance();
		// Person.class.getDeclaredMethod("info").invoke(person);
		Person.class.getDeclaredMethod("sayHello").invoke(person);
	}

	@Test
	public void test2() throws NotFoundException, CannotCompileException,
			IOException, InstantiationException, IllegalAccessException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException {
		CtClass ctClass = ClassPool.getDefault().get(
				"com.flyingh.javassist.Calc");
		CtMethod ctMethod = ctClass.getDeclaredMethod("fact");
		ctMethod.useCflow("com.flyingh.javassist.Calc.fact");
		ctMethod.insertBefore("if($cflow(com.flyingh.javassist.Calc.fact)==0){System.out.println(\"the init parameter is:\"+$1);}System.out.println(\"cflow:\"+$cflow(com.flyingh.javassist.Calc.fact));System.out.println($1);");
		Class<?> cls = ctClass.toClass();
		Calc newInstance = (Calc) cls.newInstance();
		Method method = cls.getDeclaredMethod("fact", int.class);
		System.out.println(method.invoke(newInstance, 5));
	}

	@Test
	public void test() throws NotFoundException, CannotCompileException,
			IOException, InstantiationException, IllegalAccessException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException {
		CtClass ctClass = ClassPool.getDefault().get(
				"com.flyingh.javassist.Point");
		CtMethod ctMethod = ctClass.getDeclaredMethod("move");
		ctMethod.insertBefore("{System.out.println($0);System.out.println(this);System.out.println($1);System.out.println($2);$1=100;System.out.println($1);$1=250;System.out.println($1);}");
		ctClass.writeFile();
		Class<?> cls = ctClass.toClass();
		Object newInstance = cls.newInstance();
		Method declaredMethod = cls.getDeclaredMethod("move", int.class,
				int.class);
		declaredMethod.invoke(newInstance, 3, 5);
	}
}
