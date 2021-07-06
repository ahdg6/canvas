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

package grondag.canvas.render.region;

import java.nio.IntBuffer;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import grondag.canvas.buffer.VboBuffer;
import grondag.canvas.buffer.encoding.ArrayVertexCollector;
import grondag.canvas.buffer.encoding.VertexCollectorList;
import grondag.canvas.buffer.format.CanvasVertexFormats;
import grondag.canvas.config.Configurator;
import grondag.canvas.material.property.MaterialTarget;
import grondag.canvas.material.state.RenderState;
import grondag.canvas.vf.Vf;
import grondag.canvas.vf.VfBufferReference;

// WIP: need this thing?
public class DrawableRegion {
	public static DrawableRegion EMPTY_DRAWABLE = new DrawableRegion.Dummy();
	private final VboBuffer vboBuffer;
	protected boolean isClosed = false;
	protected DrawableDelegate delegate;

	protected DrawableRegion(@Nullable VboBuffer vboBuffer, DrawableDelegate delegate) {
		this.vboBuffer = vboBuffer;
		this.delegate = delegate;
	}

	public DrawableDelegate delegate() {
		return delegate;
	}

	/**
	 * Called when buffer content is no longer current and will not be rendered.
	 */
	public void close() {
		if (!isClosed) {
			isClosed = true;

			assert delegate != null;
			delegate.release();
			delegate = null;

			if (vboBuffer != null) {
				vboBuffer.close();
			}
		}
	}

	public boolean isClosed() {
		return isClosed;
	}

	private static class Dummy extends DrawableRegion {
		protected Dummy() {
			super(null, null);
			isClosed = true;
		}

		@Override
		public DrawableDelegate delegate() {
			return null;
		}

		@Override
		public void close() {
			// NOOP
		}
	}

	private static final Predicate<RenderState> TRANSLUCENT = m -> m.target == MaterialTarget.TRANSLUCENT && m.primaryTargetTransparency;
	private static final Predicate<RenderState> SOLID = m -> !TRANSLUCENT.test(m);

	public static DrawableRegion pack(VertexCollectorList collectorList, VboBuffer vboBuffer, boolean translucent, int byteCount) {
		final ObjectArrayList<ArrayVertexCollector> drawList = collectorList.sortedDrawList(translucent ? TRANSLUCENT : SOLID);

		if (drawList.isEmpty()) {
			return EMPTY_DRAWABLE;
		}

		final ArrayVertexCollector collector = drawList.get(0);

		// WIP: restore ability to have more than one pass in non-translucent terrain, for decals, etc.
		assert drawList.size() == 1;
		assert collector.renderState.sorted == translucent;

		// WIP: ugly
		VfBufferReference vfbr = null;

		if (Configurator.vf) {
			final int quadIntCount = collector.quadCount() * CanvasVertexFormats.MATERIAL_FORMAT_VF.vertexStrideInts;
			final int[] vfData = new int[quadIntCount];
			System.arraycopy(collector.data(), 0, vfData, 0, quadIntCount);
			vfbr = VfBufferReference.of(vfData);
			Vf.QUADS.enqueue(vfbr);
		} else {
			final IntBuffer intBuffer = vboBuffer.intBuffer();
			intBuffer.position(0);
			collector.toBuffer(intBuffer);
		}

		final DrawableDelegate delegate = DrawableDelegate.claim(collector.renderState, 0, collector.quadCount() * 4, vfbr);
		return new DrawableRegion(vboBuffer, delegate);
	}

	public void bindIfNeeded() {
		if (vboBuffer != null) {
			vboBuffer.bind();
		}
	}
}
