package rest.client;

import org.junit.Assert;
import org.junit.Test;

import com.github.hipchat.integration.Converter;

public class ConverterTest {
	@Test
	public void oneProperty() {
		String convert = Converter.convert("/testrail get name of user 1");
		Assert.assertEquals("get('name') of user(1)", convert);
	}

	@Test
	public void multipleProperties() {
		String convert = Converter.convert("/testrail get name, email of user 1");
		Assert.assertEquals("get('name','email') of user(1)", convert);
	}

	@Test
	public void stringAsIdentifier() {
		String convert = Converter.convert("/testrail get name, email of user andreas@cetrea.com");
		Assert.assertEquals("get('name','email') of user('andreas@cetrea.com')", convert);
	}
}
