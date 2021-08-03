/*
 *  Copyright 2019, 2020 grondag
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package grondag.canvas.render.terrain.vbo;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import grondag.canvas.buffer.format.CanvasVertexFormats;
import grondag.canvas.buffer.input.ArrayVertexCollector;
import grondag.canvas.buffer.input.VertexCollectorList;
import grondag.canvas.buffer.render.StaticDrawBuffer;
import grondag.canvas.buffer.render.TransferBuffer;
import grondag.canvas.buffer.render.TransferBuffers;
import grondag.canvas.material.state.TerrainRenderStates;
import grondag.canvas.render.terrain.base.AbstractDrawableRegion;
import grondag.canvas.render.terrain.base.DrawableRegion;
import grondag.canvas.render.terrain.base.UploadableRegion;

public class VboDrawableRegion extends AbstractDrawableRegion<StaticDrawBuffer> {
	private VboDrawableRegion(long packedOriginBlockPos, int quadVertexCount, StaticDrawBuffer storage) {
		super(packedOriginBlockPos, quadVertexCount, storage);
	}

	public static UploadableRegion uploadable(VertexCollectorList collectorList, boolean translucent, int bytes, long packedOriginBlockPos) {
		final ObjectArrayList<ArrayVertexCollector> drawList = collectorList.sortedDrawList(translucent ? TerrainRenderStates.TRANSLUCENT_PREDICATE : TerrainRenderStates.SOLID_PREDICATE);

		if (drawList.isEmpty()) {
			return UploadableRegion.EMPTY_UPLOADABLE;
		}

		final ArrayVertexCollector collector = drawList.get(0);

		// WIP: restore ability to have more than one pass in non-translucent terrain, for decals, etc.
		assert drawList.size() == 1;
		assert collector.renderState.sorted == translucent;

		TransferBuffer buffer = TransferBuffers.claim(bytes);
		StaticDrawBuffer storage = new StaticDrawBuffer(CanvasVertexFormats.STANDARD_MATERIAL_FORMAT, buffer);
		assert storage.capacityBytes() >= buffer.sizeBytes();
		collector.toBuffer(0, buffer, 0);
		return new VboDrawableRegion(packedOriginBlockPos, collector.quadCount() * 4, storage);
	}

	@Override
	public DrawableRegion produceDrawable() {
		storage.upload();
		return this;
	}

	@Override
	protected void closeInner() {
		// NOOP
	}
}
