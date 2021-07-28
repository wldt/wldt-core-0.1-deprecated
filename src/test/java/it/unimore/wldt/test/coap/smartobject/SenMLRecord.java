package it.unimore.wldt.test.coap.smartobject;


/**
+---------------+------+---------+
|         SenML | JSON | Type    |
+---------------+------+---------+
|     Base Name | bn   | String  |
|     Base Time | bt   | Number  |
|     Base Unit | bu   | String  |
|    Base Value | bv   | Number  |
|       Version | bver | Number  |
|          Name | n    | String  |
|          Unit | u    | String  |
|         Value | v    | Number  |
|  String Value | vs   | String  |
| Boolean Value | vb   | Boolean |
|    Data Value | vd   | String  |
|     Value Sum | s    | Number  |
|          Time | t    | Number  |
|   Update Time | ut   | Number  |
+---------------+------+---------+
*/

public class SenMLRecord {

	private String bn;

	private Number bt;

	private String bu;

	private Number bv, bver;

	private String n, u;

	private Number v;

	private Number r, g, b;

	private String vs;

	private Boolean vb;

	private String vd;

	private Number s;

	private Number t;

	private Number ut;

	public SenMLRecord() {
	}

	public SenMLRecord(String bn, Number bt, int r, int g, int b,  String bu, Number bv, Number bver, String n, String u, Number v, String vs, Boolean vb, String vd, Number s, Number t, Number ut) {
		this.bn = bn;
		this.bt = bt;
		this.bu = bu;
		this.bv = bv;
		this.bver = bver;
		this.n = n;
		this.u = u;
		this.v = v;
		this.vs = vs;
		this.vb = vb;
		this.vd = vd;
		this.s = s;
		this.t = t;
		this.ut = ut;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public String getBaseName() {
		return bn;
	}

	public void setBaseName(String bn) {
		this.bn = bn;
	}

	public Number getBaseTime() {
		return bt;
	}

	public void setBaseTime(Number bt) {
		this.bt = bt;
	}

	public String getBaseUnit() {
		return bu;
	}

	public void setBaseUnit(String bu) {
		this.bu = bu;
	}

	public Number getBaseValue() {
		return bv;
	}

	public void setBaseValue(Number bv) {
		this.bv = bv;
	}

	public Number getVersion() {
		return bver;
	}

	public void setVersion(Number bver) {
		this.bver = bver;
	}

	public void setR(int r){
		this.r = r;
	}
	public void setG(int g){
		this.g = g;
	}

	public void setB(int b){
		this.b = b;
	}


	public String getName() {
		return n;
	}

	public void setName(String n) {
		this.n = n;
	}

	public String getUnit() {
		return u;
	}

	public void setUnit(String u) {
		this.u = u;
	}

	public Number getValue() {
		return v;
	}

	public void setValue(Number v) {
		this.v = v;
	}

	public String getStringValue() {
		return vs;
	}

	public void setStringValue(String vs) {
		this.vs = vs;
	}

	public Boolean getBooleanValue() {
		return vb;
	}

	public void setBooleanValue(Boolean vb) {
		this.vb = vb;
	}

	public String getDataValue() {
		return vd;
	}

	public void setDataValue(String vd) {
		this.vd = vd;
	}

	public Number getValueSum() {
		return s;
	}

	public void setValueSum(Number s) {
		this.s = s;
	}

	public Number getTime() {
		return t;
	}

	public void setTime(Number t) {
		this.t = t;
	}

	public Number getUpdateTime() {
		return ut;
	}

	public void setUpdateTime(Number ut) {
		this.ut = ut;
	}

	@Override
	public String toString() {
		return "SenMLRecord{" +
				"bn='" + bn + '\'' +
				", bt=" + bt +
				", bu='" + bu + '\'' +
				", bv=" + bv +
				", bver=" + bver +
				", n='" + n + '\'' +
				", u='" + u + '\'' +
				", v=" + v +
				", r=" + r +
				", g=" + g +
				", b=" + b +
				", vs='" + vs + '\'' +
				", vb=" + vb +
				", vd='" + vd + '\'' +
				", s=" + s +
				", t=" + t +
				", ut=" + ut +
				'}';
	}
}