/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.render;

import android.opengl.GLES20;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.Disposable;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.Cacheable;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWRuntimeException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;

import java.io.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class GpuProgram implements Cacheable, Disposable
{
    public static class GpuProgramSource
    {
        protected String vertexSource;
        protected String fragmentSource;

        public GpuProgramSource(String vertexSource, String fragmentSource)
        {
            if (WWUtil.isEmpty(vertexSource))
            {
                String msg = Logging.getMessage("nullValue.VertexSourceIsNull");
                Logging.error(msg);
                throw new IllegalArgumentException(msg);
            }

            if (WWUtil.isEmpty(fragmentSource))
            {
                String msg = Logging.getMessage("nullValue.FragmentSourceIsNull");
                Logging.error(msg);
                throw new IllegalArgumentException(msg);
            }

            this.vertexSource = vertexSource;
            this.fragmentSource = fragmentSource;
        }

        public String getVertexSource()
        {
            return this.vertexSource;
        }

        public String getFragmentSource()
        {
            return this.fragmentSource;
        }
    }

    protected int programId;
    protected GpuShader vertexShader;
    protected GpuShader fragmentShader;
    protected Map<String, Integer> attribLocations;
    protected Map<String, Integer> uniformLocations;
    protected float[] uniformArray;

    public GpuProgram(GpuProgramSource source)
    {
        if (source == null)
        {
            String msg = Logging.getMessage("nullValue.VertexSourceIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (WWUtil.isEmpty(source.getVertexSource()))
        {
            String msg = Logging.getMessage("nullValue.VertexSourceIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (WWUtil.isEmpty(source.getFragmentSource()))
        {
            String msg = Logging.getMessage("nullValue.FragmentSourceIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.initWithSource(source.getVertexSource(), source.getFragmentSource());
    }

    public GpuProgram(String vertexSource, String fragmentSource)
    {
        if (WWUtil.isEmpty(vertexSource))
        {
            String msg = Logging.getMessage("nullValue.VertexSourceIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (WWUtil.isEmpty(fragmentSource))
        {
            String msg = Logging.getMessage("nullValue.FragmentSourceIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.initWithSource(vertexSource, fragmentSource);
    }

    public GpuProgram(int programId)
    {
        if (programId <= 0)
        {
            String msg = Logging.getMessage("GL.GLObjectIsInvalid", programId);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.programId = programId;
        this.init();
    }

    protected void initWithSource(String vertexSource, String fragmentSource)
    {
        GpuShader vertexShader = null;
        GpuShader fragmentShader = null;

        try
        {
            vertexShader = new GpuShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            fragmentShader = new GpuShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        }
        catch (RuntimeException e)
        {
            if (vertexShader != null)
                vertexShader.dispose();
            if (fragmentShader != null)
                fragmentShader.dispose();

            throw e;
        }

        int program = GLES20.glCreateProgram();
        if (program <= 0)
        {
            String msg = Logging.getMessage("GL.UnableToCreateObject", Logging.getMessage("term.Program"));
            Logging.error(msg);
            throw new WWRuntimeException(msg);
        }

        GLES20.glAttachShader(program, vertexShader.getShaderId());
        GLES20.glAttachShader(program, fragmentShader.getShaderId());

        if (!this.link(program))
        {
            // Get the info log before deleting the program object.
            String infoLog = GLES20.glGetProgramInfoLog(program);
            GLES20.glDetachShader(program, vertexShader.getShaderId());
            GLES20.glDetachShader(program, fragmentShader.getShaderId());
            GLES20.glDeleteProgram(program);
            vertexShader.dispose();
            fragmentShader.dispose();

            String msg = Logging.getMessage("GL.UnableToLinkProgram", infoLog);
            Logging.error(msg);
            throw new WWRuntimeException(msg);
        }

        this.programId = program;
        this.fragmentShader = fragmentShader;
        this.vertexShader = vertexShader;
        this.init();
    }

    protected void init()
    {
        this.attribLocations = new HashMap<String, Integer>();
        this.uniformLocations = new HashMap<String, Integer>();
    }

    public static GpuProgramSource readProgramSource(Object vertexSource, Object fragmentSource) throws IOException
    {
        if (WWUtil.isEmpty(vertexSource))
        {
            String msg = Logging.getMessage("nullValue.VertexSourceIsNull");
            throw new IllegalArgumentException(msg);
        }

        if (WWUtil.isEmpty(fragmentSource))
        {
            String msg = Logging.getMessage("nullValue.FragmentSourceIsNull");
            throw new IllegalArgumentException(msg);
        }

        GpuProgram.GpuProgramSource programSource;

        InputStream vertexStream = null;
        InputStream fragmentStream = null;
        try
        {
            vertexStream = WWIO.openStream(vertexSource);
            fragmentStream = WWIO.openStream(fragmentSource);

            String vertexString = WWIO.readTextStream(vertexStream, null);
            String fragmentString = WWIO.readTextStream(fragmentStream, null);

            if (WWUtil.isEmpty(vertexString))
            {
                String msg = Logging.getMessage("GL.SourceFileIsEmpty", Logging.getMessage("term.VertexShader"),
                    vertexSource);
                throw new WWRuntimeException(msg);
            }

            if (WWUtil.isEmpty(fragmentString))
            {
                String msg = Logging.getMessage("GL.SourceFileIsEmpty", Logging.getMessage("term.FragmentShader"),
                    fragmentSource);
                throw new WWRuntimeException(msg);
            }

            programSource = new GpuProgram.GpuProgramSource(vertexString, fragmentString);
        }
        finally
        {
            WWIO.closeStream(vertexStream, vertexSource.toString());
            WWIO.closeStream(fragmentStream, fragmentSource.toString());
        }

        return programSource;
    }

    public int getProgramId()
    {
        return this.programId;
    }

    public GpuShader getFragmentShader()
    {
        return this.fragmentShader;
    }

    public GpuShader getVertexShader()
    {
        return this.vertexShader;
    }

    public long getSizeInBytes()
    {
        long size = 0;
        size += this.vertexShader != null ? this.vertexShader.getSizeInBytes() : 0;
        size += this.fragmentShader != null ? this.fragmentShader.getSizeInBytes() : 0;
        return size;
    }

    public void bind()
    {
        GLES20.glUseProgram(this.programId);
    }

    public void dispose()
    {
        if (this.programId != 0)
        {
            if (this.vertexShader != null)
                GLES20.glDetachShader(this.programId, this.vertexShader.getShaderId());
            if (this.fragmentShader != null)
                GLES20.glDetachShader(this.programId, this.fragmentShader.getShaderId());

            GLES20.glDeleteProgram(this.programId);
            this.programId = 0;
        }

        if (this.vertexShader != null)
        {
            this.vertexShader.dispose();
            this.vertexShader = null;
        }

        if (this.fragmentShader != null)
        {
            this.fragmentShader.dispose();
            this.fragmentShader = null;
        }
    }

    public int getAttribLocation(String name)
    {
        if (WWUtil.isEmpty(name))
        {
            String msg = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Integer location = this.attribLocations.get(name);
        if (location == null)
        {
            // glGetAttribLocation returns -1 if the name does not correspond to an active attribute, or if the name
            // starts with "gl_". In either case, we store the value -1 in our map since the return value does not
            // change until the program is linked again.
            location = GLES20.glGetAttribLocation(this.programId, name);
            this.attribLocations.put(name, location);
        }

        return location;
    }

    public int getUniformLocation(String name)
    {
        if (WWUtil.isEmpty(name))
        {
            String msg = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Integer location = this.uniformLocations.get(name);
        if (location == null)
        {
            // glGetUniformLocation returns -1 if the name does not correspond to an active uniform variable, or if the
            // name starts with "gl_". In either case, we store the value -1 in our map since the return value does not
            // change until the program is linked again.
            location = GLES20.glGetUniformLocation(this.programId, name);
            this.uniformLocations.put(name, location);
        }

        return location;
    }

    public void loadUniformVec4(String name, Vec4 vec)
    {
        if (WWUtil.isEmpty(name))
        {
            String msg = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        int location = this.getUniformLocation(name);
        if (location < 0)
        {
            String msg = Logging.getMessage("GL.UniformNameIsInvalid", name);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        GLES20.glUniform4f(location, (float) vec.x, (float) vec.y, (float) vec.z, (float) vec.w);
    }

    public void loadUniformVec4(String name, double x, double y, double z, double w)
    {
        if (WWUtil.isEmpty(name))
        {
            String msg = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        int location = this.getUniformLocation(name);
        if (location < 0)
        {
            String msg = Logging.getMessage("GL.UniformNameIsInvalid", name);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        GLES20.glUniform4f(location, (float) x, (float) y, (float) z, (float) w);
    }

    public void loadUniformMatrix(String name, Matrix matrix)
    {
        if (WWUtil.isEmpty(name))
        {
            String msg = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        int location = this.getUniformLocation(name);
        if (location < 0)
        {
            String msg = Logging.getMessage("GL.UniformNameIsInvalid", name);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.uniformArray == null || this.uniformArray.length < 16)
            this.uniformArray = new float[16];
        float[] m = this.uniformArray;

        // Column 1
        m[0] = (float) matrix.m[0];
        m[1] = (float) matrix.m[4];
        m[2] = (float) matrix.m[8];
        m[3] = (float) matrix.m[12];
        // Column 2
        m[4] = (float) matrix.m[1];
        m[5] = (float) matrix.m[5];
        m[6] = (float) matrix.m[9];
        m[7] = (float) matrix.m[13];
        // Column 3
        m[8] = (float) matrix.m[2];
        m[9] = (float) matrix.m[6];
        m[10] = (float) matrix.m[10];
        m[11] = (float) matrix.m[14];
        // Column 4
        m[12] = (float) matrix.m[3];
        m[13] = (float) matrix.m[7];
        m[14] = (float) matrix.m[11];
        m[15] = (float) matrix.m[15];

        GLES20.glUniformMatrix4fv(location, 1, false, m, 0);
    }

    public void loadUniformSampler(String name, int value)
    {
        if (WWUtil.isEmpty(name))
        {
            String msg = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        int location = this.getUniformLocation(name);
        if (location < 0)
        {
            String msg = Logging.getMessage("GL.UniformNameIsInvalid", name);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        GLES20.glUniform1i(location, value);
    }

    protected boolean link(int program)
    {
        int[] linkStatus = new int[1];

        GLES20.glLinkProgram(program);
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

        return linkStatus[0] == GLES20.GL_TRUE;
    }
}
