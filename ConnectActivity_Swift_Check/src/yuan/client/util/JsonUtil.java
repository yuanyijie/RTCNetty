package yuan.client.util;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 这是一个将json字符串转换成各种所需数据结构的工具类
 * 
 * @author Jack Yuan
 *
 */
public class JsonUtil {

	/**
	 * 将json转换成所需的Map类型
	 * 
	 * @param <T>
	 *            key的类型，泛型
	 * @param <M>
	 *            value的类型，泛型
	 * @param jsonString
	 * @return
	 */
	public static <T, M> Map<T, M> toCommonMap(String jsonString) {
		Map<T, M> commonMap = JSON.parseObject(jsonString,
				new TypeReference<Map<T, M>>() {
				});
		return commonMap;
	}

	public static <T, M, N> Map<T, List<Map<M, N>>> toListMap(String jsonString) {
		Map<T, List<Map<M, N>>> listmap = JSON.parseObject(jsonString,
				new TypeReference<Map<T, List<Map<M, N>>>>() {
				});
		return listmap;
	}
	
	

//	@Test
//	/**
//	 * 单元测试函数，测试相应的转换是否正确
//	 */
//	public void testUtil() {
//		String sdpString = "{\"c89485ad\":\"offersdpoffersdp\"}";
//		String candidateString = "{\"c89485ad\":[{\"candidate\":\"candidate\",\"id\":\"1\",\"label\":\"1\"}]}";
//		Map<String,String> sdpmap=toCommonMap(sdpString);
//		System.out.println(sdpmap.toString());
//		Map<String,List<Map<String,String>>> candidatesmap=toListMap(candidateString);
//		System.out.println(candidatesmap);
//	}

}
