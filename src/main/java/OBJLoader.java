import java.io.*;
import java.util.*;

public class OBJLoader {

	private static class OBJVertex {
		int posIndex, texIndex, normIndex;

		OBJVertex(int posIndex, int texIndex, int normIndex) {
			this.posIndex = posIndex;
			this.texIndex = texIndex;
			this.normIndex = normIndex;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof OBJVertex)) return false;
			OBJVertex other = (OBJVertex) obj;
			return posIndex == other.posIndex && texIndex == other.texIndex && normIndex == other.normIndex;
		}

		@Override
		public int hashCode() {
			return Objects.hash(posIndex, texIndex, normIndex);
		}
	}

	public static Model loadModel(String fileName) throws IOException {
		List<float[]> positions = new ArrayList<>();
		List<float[]> texCoords = new ArrayList<>();
		List<float[]> normals = new ArrayList<>();

		Map<OBJVertex, Integer> uniqueVertices = new LinkedHashMap<>();
		List<Integer> indices = new ArrayList<>();

		System.out.println(new File(fileName).getAbsolutePath());
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\\s+");
				if (tokens.length == 0) continue;

				switch (tokens[0]) {
					case "v":
						positions.add(new float[]{
								Float.parseFloat(tokens[1]),
								Float.parseFloat(tokens[2]),
								Float.parseFloat(tokens[3])
						});
						break;
					case "vt":
						texCoords.add(new float[]{
								Float.parseFloat(tokens[1]),
								1.0f - Float.parseFloat(tokens[2]) // flip V
						});
						break;
					case "vn":
						normals.add(new float[]{
								Float.parseFloat(tokens[1]),
								Float.parseFloat(tokens[2]),
								Float.parseFloat(tokens[3])
						});
						break;
					case "f":
						for (int i = 1; i <= 3; i++) {
							String[] parts = tokens[i].split("/");
							int posIdx = Integer.parseInt(parts[0]) - 1;
							int texIdx = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) - 1 : 0;
							int normIdx = parts.length > 2 ? Integer.parseInt(parts[2]) - 1 : 0;

							OBJVertex vertex = new OBJVertex(posIdx, texIdx, normIdx);
							Integer index = uniqueVertices.get(vertex);
							if (index == null) {
								index = uniqueVertices.size();
								uniqueVertices.put(vertex, index);
							}
							indices.add(index);
						}
						break;
				}
			}
		}

		float[] interleaved = new float[uniqueVertices.size() * 8];
		int i = 0;
		for (OBJVertex v : uniqueVertices.keySet()) {
			float[] pos = positions.get(v.posIndex);
			float[] tex = texCoords.isEmpty() ? new float[]{0, 0} : texCoords.get(v.texIndex);
			float[] norm = normals.isEmpty() ? new float[]{0, 0, 0} : normals.get(v.normIndex);

			interleaved[i++] = pos[0];
			interleaved[i++] = pos[1];
			interleaved[i++] = pos[2];
			interleaved[i++] = tex[0];
			interleaved[i++] = tex[1];
			interleaved[i++] = norm[0];
			interleaved[i++] = norm[1];
			interleaved[i++] = norm[2];
		}

		int[] indexArray = indices.stream().mapToInt(Integer::intValue).toArray();
		return new Model(interleaved, indexArray);
	}
}