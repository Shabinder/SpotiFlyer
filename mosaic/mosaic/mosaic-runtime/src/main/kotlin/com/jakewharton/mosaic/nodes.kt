package com.jakewharton.mosaic

import androidx.compose.runtime.AbstractApplier

internal sealed class MosaicNode {
	// These two values are set by a call to `measure`.
	var width = 0
	var height = 0

	// These two values are set by a call to `layout` on the parent node.
	/** Pixels right relative to parent at which this node will render. */
	var x = 0
	/** Pixels down relative to parent at which this node will render. */
	var y = 0

	/** Measure this node (and any children) and update [width] and [height]. */
	abstract fun measure()
	/** Layout any children nodes and update their [x] and [y] relative to this node. */
	abstract fun layout()
	abstract fun renderTo(canvas: TextCanvas)

	fun render(): String {
		measure()
		layout()
		val canvas = TextSurface(width, height)
		renderTo(canvas)
		return canvas.toString()
	}
}

internal class TextNode(initialValue: String = "") : MosaicNode() {
	private var sizeInvalidated = true
	var value: String = initialValue
		set(value) {
			field = value
			sizeInvalidated = true
		}

	var foreground: Color? = null
	var background: Color? = null
	var style: TextStyle? = null

	override fun measure() {
		if (sizeInvalidated) {
			val lines = value.split('\n')
			width = lines.maxOf { it.codePointCount(0, it.length) }
			height = lines.size
			sizeInvalidated = false
		}
	}

	override fun layout() {
		// No children.
	}

	override fun renderTo(canvas: TextCanvas) {
		value.split('\n').forEachIndexed { index, line ->
			canvas.write(index, 0, line, foreground, background, style)
		}
	}

	override fun toString() = "Text(\"$value\", x=$x, y=$y, width=$width, height=$height)"
}

internal class BoxNode : MosaicNode() {
	val children = mutableListOf<MosaicNode>()
	/** If row, otherwise column. */
	var isRow = true

	override fun measure() {
		if (isRow) {
			measureRow()
		} else {
			measureColumn()
		}
	}

	private fun measureRow() {
		var width = 0
		var height = 0
		for (child in children) {
			child.measure()
			width += child.width
			height = maxOf(height, child.height)
		}
		this.width = width
		this.height = height
	}

	private fun measureColumn() {
		var width = 0
		var height = 0
		for (child in children) {
			child.measure()
			width = maxOf(width, child.width)
			height += child.height
		}
		this.width = width
		this.height = height
	}

	override fun layout() {
		if (isRow) {
			layoutRow()
		} else {
			layoutColumn()
		}
	}

	private fun layoutRow() {
		var childX = 0
		for (child in children) {
			child.x = childX
			child.y = 0
			child.layout()
			childX += child.width
		}
	}

	private fun layoutColumn() {
		var childY = 0
		for (child in children) {
			child.x = 0
			child.y = childY
			child.layout()
			childY += child.height
		}
	}

	override fun renderTo(canvas: TextCanvas) {
		for (child in children) {
			val left = child.x
			val top = child.y
			val right = left + child.width - 1
			val bottom = top + child.height - 1
			child.renderTo(canvas[top..bottom, left..right])
		}
	}

	override fun toString() = children.joinToString(prefix = "Box(", postfix = ")")
}

internal class MosaicNodeApplier(root: BoxNode) : AbstractApplier<MosaicNode>(root) {
	override fun insertTopDown(index: Int, instance: MosaicNode) {
		// Ignored, we insert bottom-up.
	}

	override fun insertBottomUp(index: Int, instance: MosaicNode) {
		val boxNode = current as BoxNode
		boxNode.children.add(index, instance)
	}

	override fun remove(index: Int, count: Int) {
		val boxNode = current as BoxNode
		boxNode.children.remove(index, count)
	}

	override fun move(from: Int, to: Int, count: Int) {
		val boxNode = current as BoxNode
		boxNode.children.move(from, to, count)
	}

	override fun onClear() {
		val boxNode = root as BoxNode
		boxNode.children.clear()
	}
}
