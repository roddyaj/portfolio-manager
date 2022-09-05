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

	const calls = optionPositions.filter(p => p.symbol.endsWith("C"));
	const puts = optionPositions.filter(p => p.symbol.endsWith("P"));

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">{`${isLong ? "Long" : "Short"} Options`}</div>
				<span style={{ marginLeft: 10 }}>({optionPositions.length})</span>
			</div>
			<table>
				<thead>
					<tr>
						<th className="l">Ticker</th>
						<th>#</th>
						<th className="c">T</th>
						<th className="c">Expiry</th>
						<th>Strike</th>
						<th>Price</th>
						<th className="c">ITM</th>
						<th>Prem.</th>
					</tr>
				</thead>
				<tbody>
					{calls.map(renderRow)}
					{puts.map(renderRow)}
				</tbody>
			</table>
		</div>
	);
}

function renderRow(position, i) {
	const [symbol, expiry, strike, type] = position.symbol.split(" ");
	const rowStyle = i === 0 && type === "P" ? { borderTop: "1px solid grey" } : null;
	return (
		<tr key={position.symbol} style={rowStyle}>
			<td className="l">
				<a href={`https://finance.yahoo.com/quote/${symbol}`}>{symbol}</a>
			</td>
			<td>{Math.abs(position.quantity)}</td>
			<td className="c">{type}</td>
			<td className="l">{expiry} ({position.dte})</td>
			<td>{strike}</td>
			<td>{position.underlyingPrice ? position.underlyingPrice.toFixed(2) : ""}</td>
			<td className="c">{position.inTheMoney ? (<i className="bi bi-check" style={{ marginLeft: 6 }}></i>) : null}</td>
			<td>{Math.abs(position.costBasis)}</td>
		</tr>
	);
}

export default Options;
