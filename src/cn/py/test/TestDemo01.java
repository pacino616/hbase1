package cn.py.test;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.junit.Test;

public class TestDemo01 {

	@Test
	public void createTable() throws Exception {
		// --获取hbase的环境变量参数对象
		Configuration conf = HBaseConfiguration.create();

		// --设置zk集群连接地址，可以只写一个
		conf.set("hbase.zookeeper.quorum", "hadoop01:2181,hadoop02:2181,hadoop03:2181");

		HBaseAdmin admin = new HBaseAdmin(conf);
		//minor compact
		admin.compact("tab4".getBytes());
		//major compact
		admin.majorCompact("tab4".getBytes());
		
		// --创建HBase表对象，并指定表名
		HTableDescriptor table = new HTableDescriptor("tab4");
		// --创建HBase表的列族对象
		HColumnDescriptor cf1 = new HColumnDescriptor("cf1");
		HColumnDescriptor cf2 = new HColumnDescriptor("cf2");

		// --指定某个列族的cell最多保留的历史版本数。默认是3
		cf1.setMaxVersions(3);
		// --将列族和表产生绑定关系
		table.addFamily(cf1);
		table.addFamily(cf2);

		// --执行建表
		admin.createTable(table);

		admin.close();
	}

	@Test
	public void putdata() throws Exception {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "192.168.80.72:2181," + "192.168.80.73:2181," + "192.168.80.74:2181");

		// --创建要操作的表对象
		HTable table = new HTable(conf, "tab1");
		// --获取行对象，并指定行键
		Put row = new Put("row1".getBytes());
		// --定义此行要插入的列族和列的数据
		row.add("cf1".getBytes(), "name".getBytes(), "tom".getBytes());
		row.add("cf1".getBytes(), "age".getBytes(), "23".getBytes());
		row.add("cf2".getBytes(), "gender".getBytes(), "man".getBytes());

		// --执行插入
		table.put(row);
		table.close();
	}

	@Test
	public void put100Rows() throws Exception {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "192.168.80.72:2181," + "192.168.80.73:2181," + "192.168.80.74:2181");

		HTable table = new HTable(conf, "tab2");

		// --HBase会按rowkey做字典序排序
		for (int i = 1; i <= 100; i++) {
			Put row = new Put(("row" + i).getBytes());
			row.add("cf1".getBytes(), "number".getBytes(), (i + "").getBytes());
			table.put(row);
		}

		table.close();

	}

	@Test
	public void getData() throws Exception {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "192.168.80.72:2181," + "192.168.80.73:2181," + "192.168.80.74:2181");

		HTable table = new HTable(conf, "tab1");

		// --指定要获取的行键数据
		// --HBase本质上是一种key-value存储系统。key是行键
		// --value是行键对应的所有列族集合
		Get get = new Get("row1".getBytes());
		// --执行查询，并返回结果集
		Result result = table.get(get);

		byte[] name = result.getValue("cf1".getBytes(), "name".getBytes());

		byte[] age = result.getValue("cf1".getBytes(), "age".getBytes());

		byte[] gender = result.getValue("cf2".getBytes(), "gender".getBytes());

		System.out.println(new String(name) + new String(age) + new String(gender));

		table.close();
	}

	@Test
	public void scantable() throws Exception {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "192.168.80.72:2181," + "192.168.80.73:2181," + "192.168.80.74:2181");

		HTable table = new HTable(conf, "tab2");

		// --获取表扫描对象，可以通过此对象扫描整表
		Scan scan = new Scan();
		// --指定扫描的起始行键
		// scan.setStartRow("row1".getBytes());
		// --指定扫描的终止行键位置，不含终止位置
		// scan.setStopRow("row30".getBytes());

		// --只指定起始位置，则从起始位置到整表的最后数据都拿到
		scan.setStartRow("row30".getBytes());

		// --执行整表扫描,并返回结果集
		ResultScanner result = table.getScanner(scan);
		Iterator<Result> it = result.iterator();

		while (it.hasNext()) {
			Result r = it.next();
			byte[] number = r.getValue("cf1".getBytes(), "number".getBytes());

			System.out.println(new String(number));
		}
		table.close();
	}

	@Test
	public void deleteRow() throws Exception {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "192.168.80.72:2181," + "192.168.80.73:2181," + "192.168.80.74:2181");
		HTable table = new HTable(conf, "tab1");

		Delete delete = new Delete("row1".getBytes());
		// --执行删除
		table.delete(delete);

		table.close();

	}

	@Test
	public void droptable() throws Exception {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "192.168.80.72:2181," + "192.168.80.73:2181," + "192.168.80.74:2181");
		HBaseAdmin admin = new HBaseAdmin(conf);
		// --禁用表
		admin.disableTable("tab1");
		// --删除表
		admin.deleteTable("tab1");

		admin.close();
	}
	
	@Test
	public void regexFilter() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "192.168.80.72:2181," + "192.168.80.73:2181," + "192.168.80.74:2181");
		HTable table = new HTable(conf,"tab2");
		Scan scan = new Scan();
//		scan.setStartRow("row30".getBytes());
//		scan.setStartRow("row60".getBytes());
		//正则匹配过滤器，下例是匹配行键中含3的行键
//		Filter filter = new RowFilter(CompareOp.EQUAL,new RegexStringComparator("^.3.*$"));
		//行键比较过滤器，比较规则：1.等于；2.大于；3.小于；4.大于等于；5.小于等于
		//可以和行起始位置结合使用
//		Filter filter = new RowFilter(CompareOp.LESS_OR_EQUAL,new BinaryComparator("row90".getBytes()));
		//行键前缀过滤器
//		Filter filter = new PrefixFilter("row3".getBytes());
		//列值过滤器（匹配列值的）
		Filter filter = new SingleColumnValueFilter("cf1".getBytes(), "name".getBytes(),
				CompareOp.EQUAL, "rose".getBytes());
		
		scan.setFilter(filter);
		ResultScanner result = table.getScanner(scan);
		
		Iterator<Result> it = result.iterator();
		
		while(it.hasNext()){
			Result r = it.next();
			byte[] number = r.getValue("cf1".getBytes(), "number".getBytes());
			System.out.println(new String(number));
		}
		table.close();
	}
	
	
	
}
