package net.ninjacat.mk54.test;

class ByteArrayClassLoader extends ClassLoader {

    private final byte[] classData;

    ByteArrayClassLoader(final byte[] classData) {
        this.classData = classData;
    }

    public Class findClass(final String name) {

        return defineClass(name, this.classData, 0, this.classData.length);
    }

}
