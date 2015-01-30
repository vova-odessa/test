package nntest.data;

import nntest.interfaces.INeuron;

public class NeuronData extends Data<INeuron> {

	public NeuronData(INeuron value) {
		super(value);
	}

	@Override
	public int intValue() {
		return value.getId();
	}

	@Override
	public long longValue() {
		return (long)value.getId();
	}

	@Override
	public float floatValue() {
		return (float)value.getId();
	}

	@Override
	public double doubleValue() {
		return (double)value.getId();
	}

}
