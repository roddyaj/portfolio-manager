function Income(props) {
	const { portfolio } = props;
	const { income } = portfolio;

	if (!income || !income.length) {
		return null;
	}

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">Income</div>
			</div>
			<table>
				<thead>
					<tr>
						<th className="l">Month</th>
						<th>Options</th>
						<th>Dividend</th>
						<th>Contrib</th>
					</tr>
				</thead>
				<tbody>
					{income.map(renderRow)}
				</tbody>
			</table>
		</div>
	);
}

function renderRow(monthIncome) {
	const { month, optionsIncome, dividendIncome, contrib } = monthIncome;
	return (
		<tr key={month}>
			<td className="l">{month}</td>
			<td>{optionsIncome.toFixed(2)}</td>
			<td>{dividendIncome.toFixed(2)}</td>
			<td>{contrib.toFixed(2)}</td>
		</tr>
	);
}

export default Income;
