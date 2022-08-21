function Options(props) {
	const { portfolio, isLong } = props;

	const optionPositions = portfolio.positions
		.filter(p => p.symbol.includes(" ") && ((!isLong && p.quantity < 0) || (isLong && p.quantity > 0)))
		.sort((a, b) => {
			const [symbolA, , , typeA] = a.symbol.split(" ");
			const [symbolB, , , typeB] = b.symbol.split(" ");
			const value = typeA.localeCompare(typeB);
			return value === 0 ? symbolA.localeCompare(symbolB) : value;
		});

	if (optionPositions.length === 0) {
		return null;
	}

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">{`${isLong ? "Long" : "Short"} Options (${optionPositions.length})`}</div>
			</div>
			<table>
				<thead>
					<tr>
						<th className="l">Ticker</th>
						<th>#</th>
						<th className="c">T</th>
						<th className="c">Expiry</th>
						<th>Strike</th>
					</tr>
				</thead>
				<tbody>
					{optionPositions.map(renderRow)}
				</tbody>
			</table>
		</div>
	);
}

function renderRow(position) {
	const [symbol, expiry, strike, type] = position.symbol.split(" ");
	return (
		<tr key={position.symbol}>
			<td className="l">
				<a href={`https://finance.yahoo.com/quote/${symbol}`}>{symbol}</a>
			</td>
			<td>{Math.abs(position.quantity)}</td>
			<td>{type}</td>
			<td>{expiry}</td>
			<td>{strike}</td>
		</tr>
	);
}

export default Options;
