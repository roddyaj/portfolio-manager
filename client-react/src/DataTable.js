import { useEffect, useState } from "react";

function DataTable(props) {
	const { columns, records, tableState } = props;

	const [sortColumn, setSortColumn] = useState(null);
	const [sortDirection, setSortDirection] = useState(-1);
	const [sortedRecords, setSortedRecords] = useState([]);
	const [, setState] = useState();

	useEffect(() => {
		setSortedRecords(sort(records, sortColumn, sortDirection, columns));
		setState({});
	}, [records, sortColumn, sortDirection, columns]);

	function handleSort(column) {
		if (column.sortDirection) {
			setSortColumn(column.name);
			setSortDirection(column.name === sortColumn ? (sortDirection * -1) : column.sortDirection);
		}
	}

	return (
		<div>
			<table>
				<thead>
					<tr>
						{columns.map((column, i) => renderHeaderCell(column, i, sortColumn, sortDirection, handleSort))}
					</tr>
				</thead>
				<tbody>
					{sortedRecords.map((record, i) => renderRow(record, i, columns, tableState))}
				</tbody>
			</table>
		</div>
	);
}

function renderHeaderCell(column, colIndex, sortColumn, sortDirection, handleSort) {
	const key = `${column.name}-${colIndex}`;
	return (
		<th key={key} className={column.align} style={{ userSelect: "none", cursor: column.sortDirection ? "pointer" : "default" }} onClick={() => handleSort(column)}>
			{column.name}
			{column.name === sortColumn && (sortDirection === 1 ? <i className="bi bi-caret-up-fill" /> : <i className="bi bi-caret-down-fill" />)}
		</th>
	);
}

function renderRow(record, rowIndex, columns, tableState) {
	return (
		<tr key={rowIndex}>
			{columns.map((column, colIndex) => renderDataCell(record, rowIndex, column, colIndex, tableState))}
		</tr>
	);
}

function renderDataCell(record, rowIndex, column, colIndex, tableState) {
	const key = `${column.name}-${colIndex}`;
	const value = column.getValue(record);

	let component = null;
	if (column.render) {
		const renderData = { record, rowIndex, column, colIndex, key, value, tableState };
		component = column.render(renderData);
	}
	if (component === null || typeof component === "string") {
		const renderValue = typeof component === "string" ? component : value;
		component = (<td key={key} className={column.align === "r" ? null : column.align}>{renderValue}</td>);
	}
	return component;
}

function sort(records, sortColumnName, sortDirection, columns) {
	const sortColumn = columns.find(c => c.name === sortColumnName);
	return sortColumn ?
		records.sort((a, b) => {
			const aValue = sortColumn.getValue(a);
			const bValue = sortColumn.getValue(b);
			let sortValue;
			if (typeof aValue === "string" && typeof bValue === "string") {
				sortValue = aValue.localeCompare(bValue);
			} else {
				sortValue = (aValue || 0) - (bValue || 0);
			}
			return sortValue * sortDirection;
		}) :
		records;
}

export default DataTable;
