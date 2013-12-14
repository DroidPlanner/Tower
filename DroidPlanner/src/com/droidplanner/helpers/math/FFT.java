package com.droidplanner.helpers.math;


/**
 * Computes FFT using the Cooley-Tukey algorithm
 * http://stackoverflow.com/questions/9272232/fft-library-in-android-sdk
 * @author EricLarch
 */
public class FFT {

	int n, m;

	// Lookup tables. Only need to recompute when size of FFT changes.
	double[] cos;
	double[] sin;

	public FFT(int n) {
		this.n = n;
		this.m = (int) (Math.log(n) / Math.log(2));

		// Make sure n is a power of 2
		if (n != (1 << m))
			throw new RuntimeException("FFT length must be power of 2");

		// precompute tables
		cos = new double[n / 2];
		sin = new double[n / 2];

		for (int i = 0; i < n / 2; i++) {
			cos[i] = Math.cos(-2 * Math.PI * i / n);
			sin[i] = Math.sin(-2 * Math.PI * i / n);
		}

	}

	public void fft(double[] x, double[] y) {
		int i, j, k, n1, n2, a;
		double c, s, t1, t2;

		// Bit-reverse
		j = 0;
		n2 = n / 2;
		for (i = 1; i < n - 1; i++) {
			n1 = n2;
			while (j >= n1) {
				j = j - n1;
				n1 = n1 / 2;
			}
			j = j + n1;

			if (i < j) {
				t1 = x[i];
				x[i] = x[j];
				x[j] = t1;
				t1 = y[i];
				y[i] = y[j];
				y[j] = t1;
			}
		}

		// FFT
		n1 = 0;
		n2 = 1;

		for (i = 0; i < m; i++) {
			n1 = n2;
			n2 = n2 + n2;
			a = 0;

			for (j = 0; j < n1; j++) {
				c = cos[a];
				s = sin[a];
				a += 1 << (m - i - 1);

				for (k = j; k < n; k = k + n2) {
					t1 = c * x[k + n1] - s * y[k + n1];
					t2 = s * x[k + n1] + c * y[k + n1];
					x[k + n1] = x[k] - t1;
					y[k + n1] = y[k] - t2;
					x[k] = x[k] + t1;
					y[k] = y[k] + t2;
				}
			}
		}
	}
	
	public void modulo(double[] x, double[] y) {
		for (int i = 0; i < y.length; i++) {
			x[i] = Math.hypot(x[i], y[i]);			
		}
	}

	public double[] fftModulo(double[] data){
		double[] dataReal = data;
		double[] dataImg = new double[data.length];
		fft(dataReal, dataImg);
		modulo(dataReal,dataImg);
		return dataReal;
	}
}