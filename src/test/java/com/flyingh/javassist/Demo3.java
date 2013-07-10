package com.flyingh.javassist;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.junit.Test;

class Point {
	private int x;
	private int y;

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

}

public class Demo3 {

	@Test
	public void test() throws NotFoundException, CannotCompileException,
			IOException, InstantiationException, IllegalAccessException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException {
		CtClass ctClass = ClassPool.getDefault().get(
				"com.flyingh.javassist.Point");
		CtMethod ctMethod = ctClass.getDeclaredMethod("move");
		ctMethod.insertBefore("{System.out.println($1);System.out.println($2);$1=100;System.out.println($1);$1=250;System.out.println($1);}");
		ctClass.writeFile();
		Class<?> cls = ctClass.toClass();
		Object newInstance = cls.newInstance();
		Method declaredMethod = cls.getDeclaredMethod("move", int.class,
				int.class);
		declaredMethod.invoke(newInstance, 3, 5);
	}
}
