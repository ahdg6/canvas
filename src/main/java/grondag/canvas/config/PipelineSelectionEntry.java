/*
 * This file is part of Canvas Renderer and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.canvas.config;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TranslatableComponent;

import grondag.canvas.config.builder.Buttons;
import grondag.canvas.config.gui.ListItem;
import grondag.canvas.pipeline.config.PipelineDescription;

public class PipelineSelectionEntry extends ListItem {
	public final PipelineDescription pipeline;
	private final PipelineSelectionScreen owner;

	private boolean selected = false;
	private Button buttonWidget;

	public PipelineSelectionEntry(PipelineDescription pipeline, PipelineSelectionScreen owner) {
		super(pipeline.nameKey);
		this.pipeline = pipeline;
		this.owner = owner;
	}

	void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	protected void createWidget(int x, int y, int width, int height) {
		final var $this = this;
		this.buttonWidget = new Buttons.CustomButton(x, y, width, 20, new TranslatableComponent(pipeline.nameKey),
				b -> owner.onSelect($this)) {
			@Override
			public void renderButton(PoseStack poseStack, int i, int j, float f) {
				if (isHoveredOrFocused() && !selected) {
					fill(poseStack, x, y, x + width, y + height - 3, 0x33FFFFFF);
				}

				if (selected) {
					fill(poseStack, x, y, x + width, y + height - 3, 0x66FFFFFF);
//					hLine(poseStack, x, x + width - 1, y, 0x66FFFFFF);
//					vLine(poseStack, x, y, y + height - 4, 0x66FFFFFF);
//					vLine(poseStack, x + width - 1, y, y + height - 4, 0x66FFFFFF);
					hLine(poseStack, x, x + width - 1, y + height - 4, 0xFFFFFFFF);
				} else {
					hLine(poseStack, x, x + width - 1, y + height - 4, 0x33FFFFFF);
				}

				renderTitle(poseStack, i, j, f);
			}
		};

//		buttonWidget.setTooltip(new TranslatableComponent(pipeline.descriptionKey));

		add(buttonWidget);
	}
}
