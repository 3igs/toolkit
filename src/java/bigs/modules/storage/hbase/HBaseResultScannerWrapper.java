package bigs.modules.storage.hbase;

import java.io.IOException;

import bigs.api.exceptions.BIGSException;
import bigs.api.storage.Result;
import bigs.api.storage.ResultScanner;


public class HBaseResultScannerWrapper implements ResultScanner {

	org.apache.hadoop.hbase.client.ResultScanner hbaseResultScanner;
	
	public HBaseResultScannerWrapper (org.apache.hadoop.hbase.client.ResultScanner hbaseResultScanner) {
		this.hbaseResultScanner = hbaseResultScanner;
	}
	
	@Override
	public void close() {
		hbaseResultScanner.close();
	}

	@Override
	public Result next() {
		try {
			org.apache.hadoop.hbase.client.Result r = hbaseResultScanner.next();
			if (r==null) return null;
			else return new HBaseResultWrapper(r);
		} catch (IOException e) {
			throw new BIGSException ("io exception fetching next scan result: "+e.getMessage());
		}
	}

	@Override
	public Result[] next(int nbRows) {
		try {
			org.apache.hadoop.hbase.client.Result[] hr = hbaseResultScanner.next(nbRows);
			Result[] r = new Result[hr.length];
			for (int i=0; i<hr.length; i++) {
				r[i] = new HBaseResultWrapper(hr[i]);
			}
			return r;
		} catch (IOException e) {
			throw new BIGSException ("io exception fetching next scan result: "+e.getMessage());
		}
	}

}
